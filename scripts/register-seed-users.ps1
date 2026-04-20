param(
    [string]$BackendUrl = "http://localhost:8080",
    [string]$BackendLog = "C:\Users\Administrator\.cursor\projects\d-soft-lz-sports\terminals\769108.txt",
    [int]$UserCount = 10,
    [int]$AthleteCount = 6,
    [string]$OutFile = "git-ai/automation-route/runs/2026-04-18-auto-032-tokens.json"
)

$ErrorActionPreference = "Stop"

function Login-User {
    param([string]$Username, [string]$Password)
    $body = @{ username = $Username; password = $Password } | ConvertTo-Json
    $resp = Invoke-RestMethod -Uri "$BackendUrl/api/auth/login" -Method Post -ContentType "application/json" -Body $body
    if ($resp.code -ne 200) {
        throw "login failed for $Username"
    }
    return $resp
}

function Extract-CodeFromLog {
    param([string]$VerifyToken)
    $line = Select-String -Path $BackendLog -Pattern "verify_token:$VerifyToken -> (\d{6}):" | Select-Object -Last 1
    if (-not $line) { return $null }
    $m = [regex]::Match($line.Line, "-> (\d{6}):")
    if (-not $m.Success) { return $null }
    return $m.Groups[1].Value
}

$seed = Get-Date -Format "HHmmss"
$defaultPassword = "Pass12345"

$school = Login-User -Username "init_school_admin_01" -Password "Admin12345"
$super = Login-User -Username "ops_super_seed" -Password "Pass12345"
$eventAdmin = Login-User -Username "ops_event_admin_a" -Password "Pass12345"

$hSchool = @{ Authorization = "Bearer $($school.data.token)" }
$hSuper = @{ Authorization = "Bearer $($super.data.token)" }
$hEvent = @{ Authorization = "Bearer $($eventAdmin.data.token)" }

$bindBody = "[$($eventAdmin.data.id)]"
$bindResp = Invoke-RestMethod -Uri "$BackendUrl/api/admin/events/1/admins" -Method Post -Headers $hSuper -ContentType "application/json" -Body $bindBody
if ($bindResp.code -ne 200) {
    throw "bind event admin failed"
}

$users = @()
for ($i = 1; $i -le $UserCount; $i++) {
    if ($i -gt 1 -and (($i - 1) % 3 -eq 0)) {
        Start-Sleep -Seconds 65
    }

    $idx = "{0:D2}" -f $i
    $username = "autou_${seed}_$idx"
    $email = "auto${seed}${idx}@qq.com"

    $send = Invoke-RestMethod -Uri "$BackendUrl/api/auth/send-verify-code?email=$email" -Method Post
    if ($send.code -ne 200) {
        if ($send.msg -like "*频繁*" -or $send.msg -like "*429*") {
            Start-Sleep -Seconds 65
            $send = Invoke-RestMethod -Uri "$BackendUrl/api/auth/send-verify-code?email=$email" -Method Post
        }
    }
    if ($send.code -ne 200) {
        throw "send verify failed for ${email}: $($send.msg)"
    }
    $verifyToken = $send.data

    $code = $null
    for ($retry = 0; $retry -lt 30 -and -not $code; $retry++) {
        Start-Sleep -Milliseconds 300
        $code = Extract-CodeFromLog -VerifyToken $verifyToken
    }
    if (-not $code) {
        throw "cannot parse verify code for $email"
    }

    $verify = Invoke-RestMethod -Uri "$BackendUrl/api/auth/verify-code?verifyToken=$verifyToken&code=$code" -Method Post
    if ($verify.code -ne 200) {
        throw "verify-code failed for ${email}: $($verify.msg)"
    }
    $registerToken = $verify.data

    $regBody = @{
        username = $username
        password = $defaultPassword
        email = $email
    } | ConvertTo-Json
    $reg = Invoke-RestMethod -Uri "$BackendUrl/api/auth/register" -Method Post -ContentType "application/json" -Headers @{ Authorization = "Bearer $registerToken" } -Body $regBody
    if ($reg.code -ne 200) {
        throw "register failed for ${username}: $($reg.msg)"
    }

    $login = Login-User -Username $username -Password $defaultPassword

    $users += [PSCustomObject]@{
        userId = [long]$login.data.id
        username = $username
        email = $email
        password = $defaultPassword
        token = $login.data.token
        verifyToken = $verifyToken
        verifyCode = $code
        registerToken = $registerToken
    }
}

$deptId = 11
$targetAthletes = [Math]::Min($AthleteCount, $users.Count)
for ($j = 0; $j -lt $targetAthletes; $j++) {
    $u = $users[$j]
    $gender = if ($j % 2 -eq 0) { [string]([char]0x7537) } else { [string]([char]0x5973) }
    $userHeaders = @{ Authorization = "Bearer $($u.token)" }
    $profileBody = @{
        userName = $u.username
        name = "自动用户$($j + 1)"
        gender = $gender
        contact = "1380000$('{0:D4}' -f ($j + 1))"
        deptId = $deptId
    } | ConvertTo-Json
    $update = Invoke-RestMethod -Uri "$BackendUrl/api/auth/update" -Method Post -Headers $userHeaders -ContentType "application/json" -Body $profileBody
    if ($update.code -ne 200) {
        throw "profile update failed for $($u.username)"
    }

    # /api/auth/update forces re-login; refresh token before athlete apply
    $relogin = Login-User -Username $u.username -Password $u.password
    $u.token = $relogin.data.token
    $userHeaders = @{ Authorization = "Bearer $($u.token)" }

    $applyBody = @{ eventId = 1 } | ConvertTo-Json
    $apply = Invoke-RestMethod -Uri "$BackendUrl/api/athlete" -Method Post -Headers $userHeaders -ContentType "application/json" -Body $applyBody
    if ($apply.code -ne 200) {
        throw "athlete apply failed for $($u.username): $($apply.msg)"
    }
}

$appPage = Invoke-RestMethod -Uri "$BackendUrl/api/event-admin/1/athlete-applications?status=PENDING&page=1&size=200" -Method Get -Headers $hEvent
if ($appPage.code -ne 200) {
    throw "query athlete applications failed"
}

$createdIds = @($users | Select-Object -First $targetAthletes | ForEach-Object { $_.userId })
$pendingCreated = @($appPage.data.records | Where-Object { $createdIds -contains $_.userId } | ForEach-Object { $_.id })
if ($pendingCreated.Count -gt 0) {
    $approve = Invoke-RestMethod -Uri "$BackendUrl/api/event-admin/1/athlete-applications/batch-approve" -Method Post -Headers $hEvent -ContentType "application/json" -Body ($pendingCreated | ConvertTo-Json)
    if ($approve.code -ne 200) {
        throw "batch approve athlete failed"
    }
}

$result = [PSCustomObject]@{
    generatedAt = (Get-Date).ToString("s")
    eventId = 1
    users = $users
    approvedAthleteApplicationIds = $pendingCreated
}

$outAbs = Join-Path (Resolve-Path ".") $OutFile
$outDir = Split-Path -Path $outAbs -Parent
if (-not (Test-Path $outDir)) {
    New-Item -ItemType Directory -Path $outDir | Out-Null
}
$result | ConvertTo-Json -Depth 8 | Set-Content -Path $outAbs -Encoding UTF8

Write-Host "USERS_REGISTERED=$($users.Count)"
Write-Host "ATHLETE_APPROVED=$($pendingCreated.Count)"
Write-Host "TOKEN_FILE=$outAbs"
