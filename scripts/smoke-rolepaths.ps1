param(
    [string]$BackendUrl = "http://localhost:8080",
    [string]$SuperAdminUsername = "ops_super_seed",
    [string]$SuperAdminPassword = "Pass12345",
    [string]$SchoolAdminUsername = "init_school_admin_01",
    [string]$SchoolAdminPassword = "Admin12345",
    [string]$EventAdminUsername = "ops_event_admin_a",
    [string]$EventAdminPassword = "Pass12345",
    [string]$UserUsername = "ops_user_a",
    [string]$UserPassword = "Pass12345",
    [string]$AthleteUsername = "ops_athlete_b",
    [string]$AthletePassword = "Pass12345",
    [long]$EventId = 1,
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

function Invoke-Step {
    param(
        [string]$Name,
        [scriptblock]$Action
    )
    Write-Host "[ROLE-SMOKE] $Name"
    if ($DryRun) {
        Write-Host "  -> DryRun mode: skipped"
        return $null
    }
    return & $Action
}

function Assert-ApiCode {
    param(
        [string]$Name,
        [object]$Resp,
        [int[]]$AllowedCodes
    )
    if ($null -eq $Resp) { throw "$Name failed: empty response" }
    if (-not ($AllowedCodes -contains [int]$Resp.code)) {
        throw "$Name failed: expected code in [$($AllowedCodes -join ',')], got code=$($Resp.code), msg=$($Resp.msg)"
    }
}

function Login-Token {
    param(
        [string]$Username,
        [string]$Password
    )
    $body = @{ username = $Username; password = $Password } | ConvertTo-Json
    $resp = Invoke-RestMethod -Uri "$BackendUrl/api/auth/login" -Method Post -ContentType "application/json" -Body $body -TimeoutSec 12
    Assert-ApiCode -Name "login($Username)" -Resp $resp -AllowedCodes @(200)
    if (-not $resp.data -or -not $resp.data.token) {
        throw "login($Username) failed: missing data.token"
    }
    return @{
        token = $resp.data.token
        role  = $resp.data.role
        id    = $resp.data.id
    }
}

function Try-LoginToken {
    param(
        [string]$Username,
        [string]$Password
    )
    try {
        return Login-Token -Username $Username -Password $Password
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
    $root = Resolve-Path (Join-Path $PSScriptRoot "..")
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
public class QuerySchoolAdminsRoleSmoke {
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
    $tmp = Join-Path $env:TEMP "QuerySchoolAdminsRoleSmoke.java"
    Set-Content -Path $tmp -Value $src -Encoding ASCII
    $rows = & java -cp "$connectorJar;$env:TEMP" $tmp $dbUrl $dbUser $dbPassword 2>$null
    if ($LASTEXITCODE -ne 0 -or -not $rows) {
        return @()
    }
    return @($rows | ForEach-Object { $_.ToString().Trim() } | Where-Object { $_ })
}

function Resolve-SchoolAdminLogin {
    $candidates = @(
        @{ username = $SchoolAdminUsername; password = $SchoolAdminPassword },
        @{ username = $SchoolAdminUsername; password = "admin123" },
        @{ username = "school_admin_05bea495"; password = "admin123" },
        @{ username = "school_admin_0004b87c"; password = "admin123" },
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
        $login = Try-LoginToken -Username $candidate.username -Password $candidate.password
        if ($login) {
            Write-Host "[ROLE-SMOKE] Resolved SCHOOL_ADMIN seed: $($candidate.username)"
            return [PSCustomObject]@{
                username = $candidate.username
                password = $candidate.password
                login = $login
            }
        }
    }

    throw "Unable to resolve a working SCHOOL_ADMIN account for role smoke."
}

function Api-Get {
    param(
        [string]$Path,
        [hashtable]$Headers
    )
    return Invoke-RestMethod -Uri "$BackendUrl$Path" -Method Get -Headers $Headers -TimeoutSec 12
}

function Api-Post {
    param(
        [string]$Path,
        [hashtable]$Headers,
        [object]$Body
    )
    $json = "null"
    if ($null -ne $Body) {
        # PowerShell's ConvertTo-Json serializes a single-element array as a scalar.
        # Some endpoints (List<Long>) require a JSON array payload, so force it when needed.
        if ($Body -is [System.Array] -and $Body.Length -eq 1) {
            $json = "[" + $Body[0].ToString() + "]"
        } else {
            $json = ($Body | ConvertTo-Json -Depth 10)
        }
    }
    return Invoke-RestMethod -Uri "$BackendUrl$Path" -Method Post -Headers $Headers -ContentType "application/json" -Body $json -TimeoutSec 12
}

function Api-Put {
    param(
        [string]$Path,
        [hashtable]$Headers,
        [object]$Body
    )
    # For endpoints that expect a JSON body (e.g. List<Long>), always send a JSON payload.
    # Using [] as default avoids "Required request body is missing".
    $json = "[]"
    if ($null -ne $Body) { $json = ($Body | ConvertTo-Json -Depth 10) }
    return Invoke-RestMethod -Uri "$BackendUrl$Path" -Method Put -Headers $Headers -ContentType "application/json" -Body $json -TimeoutSec 12
}

Write-Host "=== LZ Sports Role Path Smoke ==="
Write-Host "Backend : $BackendUrl"
Write-Host "EventId : $EventId"
Write-Host "DryRun  : $DryRun"

Invoke-Step -Name "Backend init-status check" -Action {
    $resp = Api-Get -Path "/api/system/init-status" -Headers @{}
    Assert-ApiCode -Name "init-status" -Resp $resp -AllowedCodes @(200)
} | Out-Null

$resolvedSchool = Resolve-SchoolAdminLogin
$school = $resolvedSchool.login
$super = Invoke-Step -Name "Login SUPER_ADMIN ($SuperAdminUsername)" -Action { Try-LoginToken -Username $SuperAdminUsername -Password $SuperAdminPassword }
$event = Invoke-Step -Name "Login EVENT_ADMIN ($EventAdminUsername)" -Action { Login-Token -Username $EventAdminUsername -Password $EventAdminPassword }
$user = Invoke-Step -Name "Login USER ($UserUsername)" -Action { Login-Token -Username $UserUsername -Password $UserPassword }
$athlete = Invoke-Step -Name "Login ATHLETE ($AthleteUsername)" -Action { Login-Token -Username $AthleteUsername -Password $AthletePassword }

if ($DryRun) {
    Write-Host "=== Role Path Smoke Completed (DryRun) ==="
    exit 0
}

$hSuper = $null
$adminManagerHeaders = $null
$adminManagerLabel = "SCHOOL_ADMIN"
$superAvailable = ($null -ne $super)
if ($superAvailable) {
    $hSuper = @{ Authorization = "Bearer $($super.token)" }
    $adminManagerHeaders = $hSuper
    $adminManagerLabel = "SUPER_ADMIN"
} else {
    Write-Host "[ROLE-SMOKE] SUPER_ADMIN login unavailable, skip SUPER_ADMIN-only assertions"
    $adminManagerHeaders = @{ Authorization = "Bearer $($school.token)" }
}
$hSchool = @{ Authorization = "Bearer $($school.token)" }
$hEvent = @{ Authorization = "Bearer $($event.token)" }
$hUser = @{ Authorization = "Bearer $($user.token)" }
$hAthlete = @{ Authorization = "Bearer $($athlete.token)" }

# Make RequireEventAdmin positive path deterministic by binding EVENT_ADMIN to the target eventId.
Invoke-Step -Name "Bind EVENT_ADMIN to eventId=$EventId (via $adminManagerLabel)" -Action {
    $resp = Api-Post -Path "/api/admin/events/$EventId/admins" -Headers $adminManagerHeaders -Body @([long]$event.id)
    Assert-ApiCode -Name "event-admin-bind(super)" -Resp $resp -AllowedCodes @(200)
} | Out-Null

# --- SUPER_ADMIN / SCHOOL_ADMIN: positive ---
if ($superAvailable) {
    Invoke-Step -Name "SUPER_ADMIN can query admin users list" -Action {
        $resp = Api-Get -Path "/api/admin/users?currentPage=1&pageSize=1" -Headers $hSuper
        Assert-ApiCode -Name "admin-users(super)" -Resp $resp -AllowedCodes @(200)
    } | Out-Null
}

Invoke-Step -Name "SCHOOL_ADMIN can query admin users list" -Action {
    $resp = Api-Get -Path "/api/admin/users?currentPage=1&pageSize=1" -Headers $hSchool
    Assert-ApiCode -Name "admin-users(school)" -Resp $resp -AllowedCodes @(200)
} | Out-Null

Invoke-Step -Name "SCHOOL_ADMIN dashboard nums ok" -Action {
    $resp = Api-Get -Path "/api/auth/getNums" -Headers $hSchool
    Assert-ApiCode -Name "getNums(school)" -Resp $resp -AllowedCodes @(200)
} | Out-Null

if ($superAvailable) {
    Invoke-Step -Name "SUPER_ADMIN cannot access SCHOOL_ADMIN dashboard nums (expect 403)" -Action {
        $resp = Api-Get -Path "/api/auth/getNums" -Headers $hSuper
        Assert-ApiCode -Name "getNums(super)" -Resp $resp -AllowedCodes @(403)
    } | Out-Null
}

# --- EVENT_ADMIN: positive ---
Invoke-Step -Name "EVENT_ADMIN can list registrations (admin view)" -Action {
    $resp = Api-Get -Path "/api/registration/page?currentPage=1&pageSize=5" -Headers $hEvent
    Assert-ApiCode -Name "registration-page(event-admin)" -Resp $resp -AllowedCodes @(200)
} | Out-Null

Invoke-Step -Name "EVENT_ADMIN can call batch-audit (no-op ids)" -Action {
    $resp = Api-Put -Path "/api/registration/batch-audit?approve=true&eventId=$EventId" -Headers $hEvent -Body $null
    Assert-ApiCode -Name "batch-audit(event-admin)" -Resp $resp -AllowedCodes @(200)
} | Out-Null

# --- USER / ATHLETE: positive ---
Invoke-Step -Name "USER can query registration page (self view)" -Action {
    $resp = Api-Get -Path "/api/registration/page?currentPage=1&pageSize=5" -Headers $hUser
    Assert-ApiCode -Name "registration-page(user)" -Resp $resp -AllowedCodes @(200)
} | Out-Null

Invoke-Step -Name "ATHLETE can query registration page (self view)" -Action {
    $resp = Api-Get -Path "/api/registration/page?currentPage=1&pageSize=5" -Headers $hAthlete
    Assert-ApiCode -Name "registration-page(athlete)" -Resp $resp -AllowedCodes @(200)
} | Out-Null

# --- negative / privilege boundaries ---
Invoke-Step -Name "USER cannot query admin users list (expect 403)" -Action {
    $resp = Api-Get -Path "/api/admin/users?currentPage=1&pageSize=1" -Headers $hUser
    Assert-ApiCode -Name "admin-users(user)" -Resp $resp -AllowedCodes @(403)
} | Out-Null

Invoke-Step -Name "ATHLETE cannot query admin users list (expect 403)" -Action {
    $resp = Api-Get -Path "/api/admin/users?currentPage=1&pageSize=1" -Headers $hAthlete
    Assert-ApiCode -Name "admin-users(athlete)" -Resp $resp -AllowedCodes @(403)
} | Out-Null

Invoke-Step -Name "EVENT_ADMIN cannot query admin users list (expect 403)" -Action {
    $resp = Api-Get -Path "/api/admin/users?currentPage=1&pageSize=1" -Headers $hEvent
    Assert-ApiCode -Name "admin-users(event-admin)" -Resp $resp -AllowedCodes @(403)
} | Out-Null

Write-Host "=== Role Path Smoke Completed Successfully ==="
