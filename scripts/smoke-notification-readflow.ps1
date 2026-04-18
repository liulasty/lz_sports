param(
    [string]$BackendUrl = "http://localhost:8080",
    [string]$AccountsFile = "git-ai/automation-route/runs/business-accounts-latest.json",
    [string]$AthleteUsername = "",
    [string]$AthletePassword = "",
    [ValidateSet("EVENT_ADMIN","SCHOOL_ADMIN")]
    [string]$CrossUserRole = "EVENT_ADMIN",
    [string]$CrossUserUsername = "",
    [string]$CrossUserPassword = "",
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

function Invoke-Step {
    param(
        [string]$Name,
        [scriptblock]$Action
    )
    Write-Host "[NOTIFY-FLOW] $Name"
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

function Resolve-AccessToken {
    param([object]$RespData)
    if ($null -eq $RespData) { return $null }
    if ($RespData.token) { return [string]$RespData.token }
    if ($RespData.accessToken) { return [string]$RespData.accessToken }
    if ($RespData.data -and $RespData.data.token) { return [string]$RespData.data.token }
    return $null
}

function Login-Token {
    param(
        [string]$Username,
        [string]$Password
    )
    $body = @{ username = $Username; password = $Password } | ConvertTo-Json
    $resp = Invoke-RestMethod -Uri "$BackendUrl/api/auth/login" -Method Post -ContentType "application/json" -Body $body -TimeoutSec 12
    Assert-ApiCode -Name "login($Username)" -Resp $resp -AllowedCodes @(200)
    $token = Resolve-AccessToken -RespData $resp.data
    if (-not $token) { throw "login($Username) failed: missing token" }
    return $token
}

function Resolve-AthleteFromAccountsFile {
    if (-not $AccountsFile) { return $null }
    if (-not (Test-Path $AccountsFile)) { return $null }
    try {
        $json = Get-Content -Raw -Path $AccountsFile | ConvertFrom-Json
        foreach ($acc in $json.accounts) {
            if ($acc.role -eq "ATHLETE" -and $acc.username -and $acc.password) {
                return [PSCustomObject]@{
                    username = [string]$acc.username
                    password = [string]$acc.password
                }
            }
        }
    } catch {
        return $null
    }
    return $null
}

function Resolve-RoleFromAccountsFile {
    param([string]$Role)
    if (-not $AccountsFile) { return $null }
    if (-not (Test-Path $AccountsFile)) { return $null }
    try {
        $json = Get-Content -Raw -Path $AccountsFile | ConvertFrom-Json
        foreach ($acc in $json.accounts) {
            if ($acc.role -eq $Role -and $acc.username -and $acc.password) {
                return [PSCustomObject]@{
                    username = [string]$acc.username
                    password = [string]$acc.password
                }
            }
        }
    } catch {
        return $null
    }
    return $null
}

Write-Host "=== LZ Sports Notification Read Flow Smoke ==="
Write-Host "Backend : $BackendUrl"
Write-Host "Accounts: $AccountsFile"
Write-Host "DryRun  : $DryRun"

if (-not $AthleteUsername -or -not $AthletePassword) {
    $resolved = Resolve-AthleteFromAccountsFile
    if ($resolved) {
        $AthleteUsername = $resolved.username
        $AthletePassword = $resolved.password
        Write-Host "[NOTIFY-FLOW] Resolved ATHLETE from accounts file: $AthleteUsername"
    }
}
if (-not $AthleteUsername -or -not $AthletePassword) {
    throw "Unable to resolve ATHLETE credentials. Provide -AthleteUsername/-AthletePassword or a valid -AccountsFile."
}

if (-not $CrossUserUsername -or -not $CrossUserPassword) {
    $resolvedCross = Resolve-RoleFromAccountsFile -Role $CrossUserRole
    if ($resolvedCross) {
        $CrossUserUsername = $resolvedCross.username
        $CrossUserPassword = $resolvedCross.password
        Write-Host "[NOTIFY-FLOW] Resolved $CrossUserRole from accounts file: $CrossUserUsername"
    }
}

if ($DryRun) {
    Write-Host "=== Notification Read Flow Smoke Completed (DryRun) ==="
    exit 0
}

$athleteToken = Invoke-Step -Name "Login ATHLETE ($AthleteUsername)" -Action { Login-Token -Username $AthleteUsername -Password $AthletePassword }
$hAthlete = @{ Authorization = "Bearer $athleteToken" }

$unreadCountBefore = Invoke-Step -Name "Unread count before" -Action {
    Invoke-RestMethod -Uri "$BackendUrl/api/notification/unread-count" -Method Get -Headers $hAthlete -TimeoutSec 12
}
Assert-ApiCode -Name "unread-count(before)" -Resp $unreadCountBefore -AllowedCodes @(200)

$unreadPage = Invoke-Step -Name "Fetch unread page (pageSize=50)" -Action {
    Invoke-RestMethod -Uri "$BackendUrl/api/notification/page?currentPage=1&pageSize=50&isRead=false" -Method Get -Headers $hAthlete -TimeoutSec 12
}
Assert-ApiCode -Name "notification-page-unread" -Resp $unreadPage -AllowedCodes @(200)

# Pick one unread notification (if none, we can still validate read-all is idempotent)
$pickedId = $null
if ($unreadPage.data -and $unreadPage.data.records -and $unreadPage.data.records.Count -gt 0) {
    $pickedId = $unreadPage.data.records[0].id
}

if ($pickedId) {
    Invoke-Step -Name "Read one notification id=$pickedId" -Action {
        Invoke-RestMethod -Uri "$BackendUrl/api/notification/read/$pickedId" -Method Put -Headers $hAthlete -TimeoutSec 12
    } | Out-Null

    $unreadCountAfterOne = Invoke-Step -Name "Unread count after read(id)" -Action {
        Invoke-RestMethod -Uri "$BackendUrl/api/notification/unread-count" -Method Get -Headers $hAthlete -TimeoutSec 12
    }
    Assert-ApiCode -Name "unread-count(after-read-one)" -Resp $unreadCountAfterOne -AllowedCodes @(200)

    if ([int]$unreadCountAfterOne.data -gt [int]$unreadCountBefore.data) {
        throw "unread-count increased after read(id): before=$($unreadCountBefore.data) after=$($unreadCountAfterOne.data)"
    }
}

Invoke-Step -Name "Read all notifications" -Action {
    Invoke-RestMethod -Uri "$BackendUrl/api/notification/read-all" -Method Put -Headers $hAthlete -TimeoutSec 12
} | Out-Null

$unreadCountAfterAll = Invoke-Step -Name "Unread count after read-all" -Action {
    Invoke-RestMethod -Uri "$BackendUrl/api/notification/unread-count" -Method Get -Headers $hAthlete -TimeoutSec 12
}
Assert-ApiCode -Name "unread-count(after-read-all)" -Resp $unreadCountAfterAll -AllowedCodes @(200)
if ([int]$unreadCountAfterAll.data -ne 0) {
    throw "expected unread-count=0 after read-all, got $($unreadCountAfterAll.data)"
}

# Cross-user boundary: admin user must not be able to mark athlete's notification as read
if ($CrossUserUsername -and $CrossUserPassword -and $pickedId) {
    $crossToken = Invoke-Step -Name "Login $CrossUserRole ($CrossUserUsername) for cross-user boundary" -Action { Login-Token -Username $CrossUserUsername -Password $CrossUserPassword }
    $hCross = @{ Authorization = "Bearer $crossToken" }

    Invoke-Step -Name "Cross-user read should be denied (403)" -Action {
        try {
            Invoke-RestMethod -Uri "$BackendUrl/api/notification/read/$pickedId" -Method Put -Headers $hCross -TimeoutSec 12 | Out-Null
            throw "expected 403 but request succeeded"
        } catch {
            $raw = $_.ErrorDetails.Message
            if (-not $raw -or $raw -notmatch '"code"\s*:\s*403') {
                throw "expected 403 for cross-user read, raw=$raw"
            }
        }
    } | Out-Null
} elseif ($pickedId) {
    throw "Unable to resolve cross-user ($CrossUserRole) credentials for 403 assertion. Provide -CrossUserUsername/-CrossUserPassword or a valid -AccountsFile."
}

Write-Host "=== Notification Read Flow Smoke Completed Successfully ==="

