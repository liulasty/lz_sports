param(
    [string]$BackendUrl = "http://localhost:8080",
    [string]$AccountsFile = "git-ai/automation-route/accounts/business-accounts-latest.json",
    [string]$SchoolAdminUsername = "init_school_admin_01",
    [string]$SchoolAdminPassword = "Admin12345",
    [string]$RunId = "",
    [string]$RelatedTaskId = "AUTO-039",
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$runFolder = Join-Path $root "git-ai/automation-route/runs"

function Write-Step {
    param([string]$Msg)
    Write-Host "[USER-STATUS] $Msg"
}

function Assert-Code {
    param([string]$Name, [object]$Resp, [int[]]$Allowed)
    if ($null -eq $Resp) { throw "$Name failed: empty response" }
    if (-not ($Allowed -contains [int]$Resp.code)) {
        throw "$Name failed: expected [$($Allowed -join ',')], got code=$($Resp.code), msg=$($Resp.msg)"
    }
}

function Resolve-Token {
    param([object]$RespData)
    if ($null -eq $RespData) { return $null }
    if ($RespData -is [string]) { return $RespData }
    if ($RespData.PSObject.Properties.Name -contains "token") { return $RespData.token }
    return $null
}

function Login-Resp {
    param([string]$Username, [string]$Password)
    $body = @{ username = $Username; password = $Password } | ConvertTo-Json
    return Invoke-RestMethod -Uri "$BackendUrl/api/auth/login" -Method Post -ContentType "application/json" -Body $body -TimeoutSec 12
}

function Login-Account {
    param([string]$Username, [string]$Password)
    $resp = Login-Resp -Username $Username -Password $Password
    Assert-Code -Name "login($Username)" -Resp $resp -Allowed @(200)
    $token = Resolve-Token -RespData $resp.data
    if (-not $token) { throw "login($Username) failed: missing token" }
    return [PSCustomObject]@{
        id = [long]$resp.data.id
        role = [string]$resp.data.role
        username = $Username
        password = $Password
        token = $token
    }
}

function Try-Login {
    param([string]$Username, [string]$Password)
    try {
        return Login-Resp -Username $Username -Password $Password
    } catch {
        throw "login($Username) transport failed: $($_.Exception.Message)"
    }
}

function Api-Get {
    param([string]$Path, [hashtable]$Headers)
    return Invoke-RestMethod -Uri "$BackendUrl$Path" -Method Get -Headers $Headers -TimeoutSec 12
}

function Api-Post {
    param([string]$Path, [hashtable]$Headers)
    return Invoke-RestMethod -Uri "$BackendUrl$Path" -Method Post -Headers $Headers -TimeoutSec 12
}

function Resolve-SchoolAdminLogin {
    $candidates = @(
        @{ username = $SchoolAdminUsername; password = $SchoolAdminPassword },
        @{ username = $SchoolAdminUsername; password = "admin123" },
        @{ username = "school_admin_05bea495"; password = "admin123" },
        @{ username = "school_admin_0004b87c"; password = "admin123" },
        @{ username = "init_school_admin_01"; password = "Admin12345" },
        @{ username = "init_school_admin_01"; password = "Pass12345" }
    )

    foreach ($candidate in $candidates) {
        if (-not $candidate.username -or -not $candidate.password) { continue }
        try {
            $login = Login-Account -Username $candidate.username -Password $candidate.password
            Write-Step "Resolved SCHOOL_ADMIN seed: $($candidate.username)"
            return $login
        } catch {
            continue
        }
    }

    throw "Unable to resolve a working SCHOOL_ADMIN account for user status smoke."
}

function Resolve-TargetUser {
    param([hashtable]$Headers, [string]$Username)
    $resp = Api-Get -Path "/api/admin/users?currentPage=1&pageSize=20&keyword=$Username" -Headers $Headers
    Assert-Code -Name "admin-users($Username)" -Resp $resp -Allowed @(200)
    $record = @($resp.data.records | Where-Object { $_.username -eq $Username } | Select-Object -First 1)
    if (-not $record) {
        throw "cannot resolve target user record for $Username"
    }
    return $record
}

if (-not $RunId) {
    $RunId = (Get-Date -Format "yyyy-MM-dd") + "-auto-039-user-status"
}

$status = [ordered]@{
    school_admin_login = "pending"
    target_user_lookup = "pending"
    disable = "pending"
    disabled_login = "pending"
    enable = "pending"
    enabled_login = "pending"
    overall = "pending"
    error = ""
}

$restoreNeeded = $false
$restoreHeaders = $null
$restoreUserId = $null

Write-Host "=== LZ Sports User Status Smoke ==="
Write-Host "RunId       : $RunId"
Write-Host "BackendUrl  : $BackendUrl"
Write-Host "AccountsFile: $AccountsFile"
Write-Host "DryRun      : $DryRun"

try {
    if (-not (Test-Path $AccountsFile)) {
        throw "accounts file missing: $AccountsFile"
    }
    $accounts = Get-Content $AccountsFile -Raw | ConvertFrom-Json
    $userSeed = $accounts.accounts | Where-Object { $_.role -eq "USER" } | Select-Object -First 1
    if (-not $userSeed) {
        throw "accounts file does not contain USER account"
    }

    Write-Step "Resolve SCHOOL_ADMIN"
    $school = Resolve-SchoolAdminLogin
    $status.school_admin_login = "pass"
    $headers = @{ Authorization = "Bearer $($school.token)" }
    $restoreHeaders = $headers

    Write-Step "Lookup target USER account in admin list"
    $targetUser = Resolve-TargetUser -Headers $headers -Username ([string]$userSeed.username)
    $targetUserId = [long]$targetUser.id
    $restoreUserId = $targetUserId
    $status.target_user_lookup = "pass"

    if ($DryRun) {
        $status.disable = "skipped"
        $status.disabled_login = "skipped"
        $status.enable = "skipped"
        $status.enabled_login = "skipped"
    } else {
        Write-Step "Disable target USER"
        $disableResp = Api-Post -Path "/api/admin/users/$targetUserId/disable" -Headers $headers
        Assert-Code -Name "disable-user" -Resp $disableResp -Allowed @(200)
        $restoreNeeded = $true
        $status.disable = "pass"

        Write-Step "Disabled USER login should fail with business error"
        $disabledLogin = Try-Login -Username ([string]$userSeed.username) -Password ([string]$userSeed.password)
        Assert-Code -Name "disabled-login" -Resp $disabledLogin -Allowed @(409)
        $status.disabled_login = "pass"

        Write-Step "Re-enable target USER"
        $enableResp = Api-Post -Path "/api/admin/users/$targetUserId/enable" -Headers $headers
        Assert-Code -Name "enable-user" -Resp $enableResp -Allowed @(200)
        $restoreNeeded = $false
        $status.enable = "pass"

        Write-Step "Enabled USER login should recover"
        $enabledLogin = Login-Account -Username ([string]$userSeed.username) -Password ([string]$userSeed.password)
        if (-not $enabledLogin.token) {
            throw "enabled-login failed: missing token after enable"
        }
        $status.enabled_login = "pass"
    }

    $status.overall = "pass"
} catch {
    $status.overall = "fail"
    $status.error = $_.Exception.Message
    throw
} finally {
    if ($restoreNeeded -and $restoreHeaders -and $restoreUserId) {
        try {
            Write-Step "Restore target USER to enabled state"
            $restoreResp = Api-Post -Path "/api/admin/users/$restoreUserId/enable" -Headers $restoreHeaders
            Assert-Code -Name "restore-enable-user" -Resp $restoreResp -Allowed @(200)
        } catch {
            Write-Step "Restore failed: $($_.Exception.Message)"
        }
    }
    if (-not (Test-Path $runFolder)) {
        New-Item -ItemType Directory -Path $runFolder | Out-Null
    }
    $runPath = Join-Path $runFolder "$RunId.md"
    $escapedError = $status.error -replace "\r?\n", " "
    if ([string]::IsNullOrWhiteSpace($escapedError)) { $escapedError = "none" }
    $content = @"
run_id: $RunId
related_task_id: $RelatedTaskId
changed_files:
  - scripts/smoke-user-status.ps1
  - git-ai/automation-route/runs/$RunId.md
key_changes:
  - Added a targeted user-status smoke that disables a seeded USER account, verifies login rejection, re-enables the user, and verifies login recovery.
  - Reused the current SCHOOL_ADMIN seed resolution and business-accounts asset file so the check can run without rebuilding fixture accounts.
test_results:
  - overall: $($status.overall)
  - steps:
      school_admin_login: $($status.school_admin_login)
      target_user_lookup: $($status.target_user_lookup)
      disable: $($status.disable)
      disabled_login: $($status.disabled_login)
      enable: $($status.enable)
      enabled_login: $($status.enabled_login)
risks:
  - "This smoke mutates the seeded USER account status and assumes the enable step always runs after disable."
  - "If the run aborts between disable and enable, the seeded USER account may remain disabled until manually restored."
issue_inputs:
  - source: local-run
    signal: "$escapedError"
repeated_failures:
  - issue_key: user-status-smoke
    attempts: 1
    blocked: false
next_task: "AUTO-040 扩展业务回归到赛事管理最小闭环"
next_action: "Reuse this smoke after any admin-user status or auth-login change."
"@
    Set-Content -Path $runPath -Value $content -Encoding UTF8
    Write-Step "Run record written: $runPath"
}

if ($status.overall -eq "pass") {
    Write-Host "=== User Status Smoke Completed Successfully ==="
}
