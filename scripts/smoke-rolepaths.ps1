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

$super = Invoke-Step -Name "Login SUPER_ADMIN ($SuperAdminUsername)" -Action { Login-Token -Username $SuperAdminUsername -Password $SuperAdminPassword }
$school = Invoke-Step -Name "Login SCHOOL_ADMIN ($SchoolAdminUsername)" -Action { Login-Token -Username $SchoolAdminUsername -Password $SchoolAdminPassword }
$event = Invoke-Step -Name "Login EVENT_ADMIN ($EventAdminUsername)" -Action { Login-Token -Username $EventAdminUsername -Password $EventAdminPassword }
$user = Invoke-Step -Name "Login USER ($UserUsername)" -Action { Login-Token -Username $UserUsername -Password $UserPassword }
$athlete = Invoke-Step -Name "Login ATHLETE ($AthleteUsername)" -Action { Login-Token -Username $AthleteUsername -Password $AthletePassword }

if ($DryRun) {
    Write-Host "=== Role Path Smoke Completed (DryRun) ==="
    exit 0
}

$hSuper = @{ Authorization = "Bearer $($super.token)" }
$hSchool = @{ Authorization = "Bearer $($school.token)" }
$hEvent = @{ Authorization = "Bearer $($event.token)" }
$hUser = @{ Authorization = "Bearer $($user.token)" }
$hAthlete = @{ Authorization = "Bearer $($athlete.token)" }

# Make RequireEventAdmin positive path deterministic by binding EVENT_ADMIN to the target eventId.
Invoke-Step -Name "Bind EVENT_ADMIN to eventId=$EventId (via SUPER_ADMIN)" -Action {
    $resp = Api-Post -Path "/api/admin/events/$EventId/admins" -Headers $hSuper -Body @([long]$event.id)
    Assert-ApiCode -Name "event-admin-bind(super)" -Resp $resp -AllowedCodes @(200)
} | Out-Null

# --- SUPER_ADMIN / SCHOOL_ADMIN: positive ---
Invoke-Step -Name "SUPER_ADMIN can query admin users list" -Action {
    $resp = Api-Get -Path "/api/admin/users?currentPage=1&pageSize=1" -Headers $hSuper
    Assert-ApiCode -Name "admin-users(super)" -Resp $resp -AllowedCodes @(200)
} | Out-Null

Invoke-Step -Name "SCHOOL_ADMIN can query admin users list" -Action {
    $resp = Api-Get -Path "/api/admin/users?currentPage=1&pageSize=1" -Headers $hSchool
    Assert-ApiCode -Name "admin-users(school)" -Resp $resp -AllowedCodes @(200)
} | Out-Null

Invoke-Step -Name "SCHOOL_ADMIN dashboard nums ok" -Action {
    $resp = Api-Get -Path "/api/auth/getNums" -Headers $hSchool
    Assert-ApiCode -Name "getNums(school)" -Resp $resp -AllowedCodes @(200)
} | Out-Null

Invoke-Step -Name "SUPER_ADMIN cannot access SCHOOL_ADMIN dashboard nums (expect 403)" -Action {
    $resp = Api-Get -Path "/api/auth/getNums" -Headers $hSuper
    Assert-ApiCode -Name "getNums(super)" -Resp $resp -AllowedCodes @(403)
} | Out-Null

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

