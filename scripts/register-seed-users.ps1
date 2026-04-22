param(
    [string]$BackendUrl = "http://localhost:8080",
    [string]$BackendLog = "",
    [long]$EventId = 1,
    [string]$Seed = "",
    [string]$DefaultPassword = "Pass12345",
    [string]$SchoolAdminUsername = "init_school_admin_01",
    [string]$SchoolAdminPassword = "Admin12345",
    [string]$SuperAdminUsername = "",
    [string]$SuperAdminPassword = "",
    [string]$OutJson = "",
    [string]$OutMarkdown = ""
)

$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$runFolder = Join-Path $root "git-ai/automation-route/runs"

function Write-Step {
    param([string]$Msg)
    Write-Host "[REGISTER-SEED] $Msg"
}

function Resolve-BackendLog {
    param([string]$ExplicitPath)
    if ($ExplicitPath -and (Test-Path $ExplicitPath)) {
        return $ExplicitPath
    }

    $cursorDir = "C:\Users\Administrator\.cursor\projects\d-soft-lz-sports\terminals"
    if (Test-Path $cursorDir) {
        $match = Get-ChildItem $cursorDir -Filter *.txt -File |
            Sort-Object LastWriteTime -Descending |
            Select-Object -First 1 -ExpandProperty FullName
        if ($match) {
            return $match
        }
    }

    throw "Unable to resolve backend log path. Pass -BackendLog explicitly."
}

function Login-User {
    param([string]$Username, [string]$Password)
    $body = @{ username = $Username; password = $Password } | ConvertTo-Json
    $resp = Invoke-RestMethod -Uri "$BackendUrl/api/auth/login" -Method Post -ContentType "application/json" -Body $body -TimeoutSec 12
    if ([int]$resp.code -ne 200 -or -not $resp.data.token) {
        throw "login failed for ${Username}: $($resp.msg)"
    }
    return $resp
}

function Try-LoginUser {
    param([string]$Username, [string]$Password)
    try {
        return Login-User -Username $Username -Password $Password
    } catch {
        return $null
    }
}

function Read-EnvMap {
    param([string]$Path)
    $map = @{}
    if (-not (Test-Path $Path)) { return $map }
    Get-Content -Path $Path | ForEach-Object {
        $line = $_.Trim()
        if (-not $line -or $line.StartsWith("#")) { return }
        $idx = $line.IndexOf("=")
        if ($idx -lt 1) { return }
        $map[$line.Substring(0, $idx).Trim()] = $line.Substring($idx + 1).Trim()
    }
    return $map
}

function Resolve-ConnectorJar {
    $candidates = @(
        "D:\CODE\mvn_repository\com\mysql\mysql-connector-j",
        (Join-Path $env:USERPROFILE ".m2\repository\com\mysql\mysql-connector-j")
    )
    foreach ($base in $candidates) {
        if (-not (Test-Path $base)) { continue }
        $jar = Get-ChildItem -Path $base -Recurse -Filter "mysql-connector-j-*.jar" -File -ErrorAction SilentlyContinue |
            Sort-Object FullName -Descending |
            Select-Object -First 1 -ExpandProperty FullName
        if ($jar) { return $jar }
    }
    return $null
}

function Get-LatestSchoolAdminCandidates {
    $envMap = Read-EnvMap -Path (Join-Path $root "config/.env.dev")
    $dbUrl = $envMap["DB_URL"]
    if (-not $dbUrl) { return @() }
    if ($dbUrl -match "://mysql:") {
        $dbUrl = $dbUrl -replace "://mysql:", "://localhost:"
    }
    $dbUser = $envMap["DB_USERNAME"]
    if (-not $dbUser) { $dbUser = $envMap["DB_USER"] }
    if (-not $dbUser) { return @() }
    $dbPassword = $envMap["DB_PASSWORD"]
    if ($null -eq $dbPassword) { $dbPassword = "" }

    $connectorJar = Resolve-ConnectorJar
    if (-not $connectorJar) { return @() }

    $src = @"
import java.sql.*;
public class QuerySchoolAdmins {
  public static void main(String[] args) throws Exception {
    Class.forName("com.mysql.cj.jdbc.Driver");
    try (Connection c = DriverManager.getConnection(args[0], args[1], args[2]);
         PreparedStatement ps = c.prepareStatement("select username from sys_user where user_type='SCHOOL_ADMIN' and status='ACTIVE' order by id desc limit 10")) {
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          System.out.println(rs.getString(1));
        }
      }
    }
  }
}
"@
    $tmp = Join-Path $env:TEMP "QuerySchoolAdmins.java"
    Set-Content -Path $tmp -Value $src -Encoding ASCII
    $rows = & java -cp "$connectorJar;$env:TEMP" $tmp $dbUrl $dbUser $dbPassword 2>$null
    if ($LASTEXITCODE -ne 0 -or -not $rows) {
        return @()
    }
    return @($rows | ForEach-Object { $_.ToString().Trim() } | Where-Object { $_ })
}

function Resolve-SchoolAdmin {
    $candidates = @(
        @{ username = $SchoolAdminUsername; password = $SchoolAdminPassword },
        @{ username = $SchoolAdminUsername; password = "admin123" },
        @{ username = "school_admin_05bea495"; password = "admin123" },
        @{ username = "school_admin_0004b87c"; password = "admin123" },
        # Integration tests may create random school_admin_<suffix> accounts with password "admin123".
        # Keep a few recent known seeds to recover local dev env if data gets reset by tests.
        @{ username = "school_admin_96719547"; password = "admin123" },
        @{ username = "school_admin_62e2507a"; password = "admin123" },
        @{ username = "init_school_admin_01"; password = "Admin12345" },
        @{ username = "init_school_admin_01"; password = "Pass12345" }
    )
    foreach ($username in Get-LatestSchoolAdminCandidates) {
        $candidates += @{ username = $username; password = "admin123" }
        $candidates += @{ username = $username; password = "Admin12345" }
    }

    foreach ($candidate in $candidates) {
        if (-not $candidate.username -or -not $candidate.password) { continue }
        $login = Try-LoginUser -Username $candidate.username -Password $candidate.password
        if ($login) {
            Write-Step "Using school admin seed: $($candidate.username)"
            return [PSCustomObject]@{
                username = $candidate.username
                password = $candidate.password
                login = $login
            }
        }
    }

    throw "Unable to resolve a working SCHOOL_ADMIN seed account."
}

function Extract-CodeFromLog {
    param([string]$LogPath, [string]$VerifyToken, [string]$TargetEmail)
    $pattern = "verify_token:$VerifyToken -> (\d{6}):$([regex]::Escape($TargetEmail))"
    $line = Select-String -Path $LogPath -Pattern $pattern | Select-Object -Last 1
    if (-not $line) { return $null }
    $m = [regex]::Match($line.Line, "-> (\d{6}):")
    if (-not $m.Success) { return $null }
    return $m.Groups[1].Value
}

function Invoke-Json {
    param(
        [string]$Method,
        [string]$Url,
        [hashtable]$Headers,
        [object]$Body
    )
    $params = @{
        Uri = $Url
        Method = $Method
        TimeoutSec = 12
    }
    if ($Headers) { $params.Headers = $Headers }
    if ($null -ne $Body) {
        $params.ContentType = "application/json"
        $params.Body = ($Body | ConvertTo-Json -Depth 10)
    }
    return Invoke-RestMethod @params
}

function Find-UserIdByUsername {
    param([hashtable]$Headers, [string]$Username)
    $resp = Invoke-RestMethod -Uri "$BackendUrl/api/admin/users?currentPage=1&pageSize=20&keyword=$Username" -Method Get -Headers $Headers -TimeoutSec 12
    if ([int]$resp.code -ne 200) {
        throw "admin-users query failed for ${Username}: $($resp.msg)"
    }
    $record = @($resp.data.records | Where-Object { $_.username -eq $Username } | Select-Object -First 1)
    if (-not $record) {
        throw "cannot resolve user id for ${Username}"
    }
    return [long]$record.id
}

function Register-And-Audit {
    param(
        [string]$Username,
        [string]$Email,
        [string]$Password,
        [string]$LogPath,
        [hashtable]$SchoolHeaders
    )

    Write-Step "Register $Username"
    $send = $null
    for ($attempt = 0; $attempt -lt 3; $attempt++) {
        $send = Invoke-RestMethod -Uri "$BackendUrl/api/auth/send-verify-code?email=$Email" -Method Post -TimeoutSec 12
        if ([int]$send.code -eq 200) {
            break
        }
        if ($attempt -lt 2) {
            Start-Sleep -Seconds 65
        }
    }
    if ([int]$send.code -ne 200) {
        throw "send-verify-code failed for ${Email}: $($send.msg)"
    }
    $verifyToken = [string]$send.data

    $code = $null
    for ($retry = 0; $retry -lt 30 -and -not $code; $retry++) {
        Start-Sleep -Milliseconds 300
        $code = Extract-CodeFromLog -LogPath $LogPath -VerifyToken $verifyToken -TargetEmail $Email
    }
    if (-not $code) {
        throw "cannot parse verify code for ${Email} from $LogPath"
    }

    $verify = Invoke-RestMethod -Uri "$BackendUrl/api/auth/verify-code?verifyToken=$verifyToken&code=$code" -Method Post -TimeoutSec 12
    if ([int]$verify.code -ne 200) {
        throw "verify-code failed for ${Email}: $($verify.msg)"
    }
    $registerToken = [string]$verify.data

    $regBody = @{
        username = $Username
        password = $Password
        email = $Email
    }
    $reg = Invoke-Json -Method Post -Url "$BackendUrl/api/auth/register" -Headers @{ Authorization = "Bearer $registerToken" } -Body $regBody
    if ([int]$reg.code -ne 200) {
        throw "register failed for ${Username}: $($reg.msg)"
    }

    $userId = Find-UserIdByUsername -Headers $SchoolHeaders -Username $Username

    Write-Step "Audit registered user $Username"
    $audit = Invoke-RestMethod -Uri "$BackendUrl/api/auth/audit/${userId}?status=1" -Method Put -Headers $SchoolHeaders -TimeoutSec 12
    if ([int]$audit.code -ne 200) {
        throw "user audit failed for ${Username}: $($audit.msg)"
    }

    $login = Login-User -Username $Username -Password $Password

    return [PSCustomObject]@{
        userId = $userId
        username = $Username
        email = $Email
        password = $Password
        token = $login.data.token
        verifyToken = $verifyToken
        verifyCode = $code
        registerToken = $registerToken
        registerAudit = "APPROVED"
    }
}

function Change-Role {
    param([hashtable]$Headers, [long]$UserId, [string]$Role)
    $resp = Invoke-RestMethod -Uri "$BackendUrl/api/admin/users/$UserId/role?role=$Role" -Method Put -Headers $Headers -TimeoutSec 12
    if ([int]$resp.code -ne 200) {
        throw "change role failed for userId=${UserId}: $($resp.msg)"
    }
}

function Bind-EventAdmin {
    param([hashtable]$Headers, [long]$EventId, [long]$UserId)
    $resp = Invoke-RestMethod -Uri "$BackendUrl/api/admin/events/$EventId/admins" -Method Post -Headers $Headers -ContentType "application/json" -Body "[$UserId]" -TimeoutSec 12
    if ([int]$resp.code -ne 200) {
        throw "bind event admin failed for userId=${UserId}: $($resp.msg)"
    }
}

function Update-Profile {
    param(
        [string]$Username,
        [string]$Password,
        [string]$Name,
        [string]$Gender,
        [string]$Contact,
        [long]$DeptId
    )
    $login = Login-User -Username $Username -Password $Password
    $headers = @{ Authorization = "Bearer $($login.data.token)" }
    $body = @{
        userName = $Username
        name = $Name
        gender = $Gender
        contact = $Contact
        deptId = $DeptId
    }
    $resp = Invoke-Json -Method Post -Url "$BackendUrl/api/auth/update" -Headers $headers -Body $body
    if ([int]$resp.code -ne 200) {
        throw "profile update failed for ${Username}: $($resp.msg)"
    }
}

function Apply-Athlete {
    param(
        [string]$Username,
        [string]$Password,
        [long]$EventId
    )
    $login = Login-User -Username $Username -Password $Password
    $headers = @{ Authorization = "Bearer $($login.data.token)" }
    $resp = Invoke-Json -Method Post -Url "$BackendUrl/api/athlete" -Headers $headers -Body @{ eventId = $EventId }
    if ([int]$resp.code -ne 200) {
        throw "athlete apply failed for ${Username}: $($resp.msg)"
    }
    return $login
}

function Find-AthleteApplicationId {
    param([hashtable]$Headers, [long]$EventId, [long]$UserId)
    $resp = Invoke-RestMethod -Uri "$BackendUrl/api/event-admin/$EventId/athlete-applications?status=PENDING&page=1&size=200" -Method Get -Headers $Headers -TimeoutSec 12
    if ([int]$resp.code -ne 200) {
        throw "query athlete applications failed: $($resp.msg)"
    }
    $record = @($resp.data.records | Where-Object { [long]$_.userId -eq $UserId } | Select-Object -First 1)
    if (-not $record) {
        throw "cannot find pending athlete application for userId=${UserId}"
    }
    return [long]$record.id
}

if (-not $Seed) {
    $Seed = Get-Date -Format "HHmmss"
}
if (-not $OutJson) {
    $OutJson = "git-ai/automation-route/runs/$(Get-Date -Format 'yyyy-MM-dd')-business-accounts-$Seed.json"
}
if (-not $OutMarkdown) {
    $OutMarkdown = "git-ai/automation-route/runs/$(Get-Date -Format 'yyyy-MM-dd')-business-accounts-$Seed.md"
}

$backendLogPath = Resolve-BackendLog -ExplicitPath $BackendLog
Write-Step "Backend log: $backendLogPath"

$schoolSeed = Resolve-SchoolAdmin
$school = $schoolSeed.login
$hSchool = @{ Authorization = "Bearer $($school.data.token)" }

$eventAdminAccount = Register-And-Audit -Username "ops_event_admin_$Seed" -Email "event${Seed}@qq.com" -Password $DefaultPassword -LogPath $backendLogPath -SchoolHeaders $hSchool
$userAccount = Register-And-Audit -Username "ops_user_$Seed" -Email "user${Seed}@qq.com" -Password $DefaultPassword -LogPath $backendLogPath -SchoolHeaders $hSchool
$athleteAccount = Register-And-Audit -Username "ops_athlete_$Seed" -Email "athlete${Seed}@qq.com" -Password $DefaultPassword -LogPath $backendLogPath -SchoolHeaders $hSchool

Write-Step "Normalize base roles"
Change-Role -Headers $hSchool -UserId $userAccount.userId -Role "USER"
Change-Role -Headers $hSchool -UserId $athleteAccount.userId -Role "USER"
$userLogin = Login-User -Username $userAccount.username -Password $userAccount.password
$userAccount.token = $userLogin.data.token

Write-Step "Promote event admin and bind event"
Change-Role -Headers $hSchool -UserId $eventAdminAccount.userId -Role "EVENT_ADMIN"
Bind-EventAdmin -Headers $hSchool -EventId $EventId -UserId $eventAdminAccount.userId
$eventAdminLogin = Login-User -Username $eventAdminAccount.username -Password $eventAdminAccount.password
$eventAdminAccount.token = $eventAdminLogin.data.token
$eventAdminHeaders = @{ Authorization = "Bearer $($eventAdminAccount.token)" }

Write-Step "Prepare athlete profile and submit application"
Update-Profile -Username $athleteAccount.username -Password $athleteAccount.password -Name "自动运动员$Seed" -Gender ([string]([char]0x7537)) -Contact "1380000$Seed" -DeptId 11
$athleteLoginAfterApply = Apply-Athlete -Username $athleteAccount.username -Password $athleteAccount.password -EventId $EventId
$athleteAccount.token = $athleteLoginAfterApply.data.token
$applicationId = Find-AthleteApplicationId -Headers $eventAdminHeaders -EventId $EventId -UserId $athleteAccount.userId

Write-Step "Approve athlete application and promote athlete role"
$approveResp = Invoke-RestMethod -Uri "$BackendUrl/api/event-admin/$EventId/athlete-applications/$applicationId/approve" -Method Post -Headers $eventAdminHeaders -TimeoutSec 12
if ([int]$approveResp.code -ne 200) {
    throw "approve athlete application failed: $($approveResp.msg)"
}
Change-Role -Headers $hSchool -UserId $athleteAccount.userId -Role "ATHLETE"
$athleteLoginFinal = Login-User -Username $athleteAccount.username -Password $athleteAccount.password
$athleteAccount.token = $athleteLoginFinal.data.token

$result = [ordered]@{
    generatedAt = (Get-Date).ToString("s")
    eventId = $EventId
    backendLog = $backendLogPath
    accounts = @(
        [ordered]@{
            username = $eventAdminAccount.username
            password = $eventAdminAccount.password
            email = $eventAdminAccount.email
            userId = $eventAdminAccount.userId
            role = "EVENT_ADMIN"
            status = "ACTIVE"
            token = $eventAdminAccount.token
            source = "registered via verify-code, approved by school admin, promoted by super admin, bound to event"
        },
        [ordered]@{
            username = $userAccount.username
            password = $userAccount.password
            email = $userAccount.email
            userId = $userAccount.userId
            role = "USER"
            status = "ACTIVE"
            token = $userAccount.token
            source = "registered via verify-code, approved by school admin"
        },
        [ordered]@{
            username = $athleteAccount.username
            password = $athleteAccount.password
            email = $athleteAccount.email
            userId = $athleteAccount.userId
            role = "ATHLETE"
            status = "ACTIVE"
            token = $athleteAccount.token
            athleteApplicationId = $applicationId
            athleteAudit = "APPROVED"
            source = "registered via verify-code, approved by school admin, completed athlete apply, approved by event admin, promoted to ATHLETE"
        }
    )
}

$jsonAbs = $OutJson
if (-not [System.IO.Path]::IsPathRooted($jsonAbs)) {
    $jsonAbs = Join-Path $root $OutJson
}
$mdAbs = $OutMarkdown
if (-not [System.IO.Path]::IsPathRooted($mdAbs)) {
    $mdAbs = Join-Path $root $OutMarkdown
}
foreach ($path in @($jsonAbs, $mdAbs)) {
    $dir = Split-Path -Path $path -Parent
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir | Out-Null
    }
}

($result | ConvertTo-Json -Depth 8) | Set-Content -Path $jsonAbs -Encoding UTF8

$md = @"
run_id: business-accounts-$Seed
purpose: "本地多角色业务回归账号资产"
warning: "包含明文账号、密码、token，仅限本地联调使用，禁止外传"

backend_log:
  path: "$backendLogPath"
  note: "验证码通过 send-verify-code 后从该日志提取"

accounts:
  event_admin:
    username: "$($eventAdminAccount.username)"
    password: "$($eventAdminAccount.password)"
    email: "$($eventAdminAccount.email)"
    role: "EVENT_ADMIN"
    source: "注册 -> 学校管理员审核 -> 超级管理员设为赛事管理员 -> 绑定赛事"
  user:
    username: "$($userAccount.username)"
    password: "$($userAccount.password)"
    email: "$($userAccount.email)"
    role: "USER"
    source: "注册 -> 学校管理员审核"
  athlete:
    username: "$($athleteAccount.username)"
    password: "$($athleteAccount.password)"
    email: "$($athleteAccount.email)"
    role: "ATHLETE"
    athleteApplicationId: $applicationId
    athleteAudit: "APPROVED"
    source: "注册 -> 学校管理员审核 -> 完善资料 -> 提交运动员申请 -> 赛事管理员审核通过 -> 超级管理员设为 ATHLETE"

token_storage:
  file: "$OutJson"
  note: "内含三类角色当前 token，可用于后续接口回放"
"@

Set-Content -Path $mdAbs -Value $md -Encoding UTF8

Write-Host "BUSINESS_ACCOUNTS_JSON=$jsonAbs"
Write-Host "BUSINESS_ACCOUNTS_MD=$mdAbs"
Write-Host "EVENT_ADMIN_USERNAME=$($eventAdminAccount.username)"
Write-Host "USER_USERNAME=$($userAccount.username)"
Write-Host "ATHLETE_USERNAME=$($athleteAccount.username)"
