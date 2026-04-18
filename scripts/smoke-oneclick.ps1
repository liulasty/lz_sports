param(
    [string]$BackendUrl = "http://localhost:8080",
    [string]$FrontendUrl = "http://localhost:5173",
    [long]$EventId = 1,
    [string]$AccountsFile = "git-ai/automation-route/runs/business-accounts-latest.json",
    [string]$Username = "ops_athlete_b",
    [string]$Password = "Pass12345",
    [string]$RunId = "",
    [string]$RelatedTaskId = "",
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$runFolder = Join-Path $root "git-ai/automation-route/runs"
$fullSmoke = Join-Path $root "scripts/smoke-full.ps1"
$roleSmoke = Join-Path $root "scripts/smoke-rolepaths.ps1"

function Write-Step {
    param([string]$Msg)
    Write-Host "[ONECLICK] $Msg"
}

function Resolve-BusinessAccountMap {
    param([string]$Path)
    if (-not $Path) { return @{} }
    $absolutePath = $Path
    if (-not [System.IO.Path]::IsPathRooted($absolutePath)) {
        $absolutePath = Join-Path $root $Path
    }
    if (-not (Test-Path $absolutePath)) {
        Write-Step "Accounts file not found, fallback to explicit credentials: $absolutePath"
        return @{}
    }

    $json = Get-Content -Path $absolutePath -Raw | ConvertFrom-Json
    $accountMap = @{}
    foreach ($account in @($json.accounts)) {
        if ($account.role -and -not $accountMap.ContainsKey([string]$account.role)) {
            $accountMap[[string]$account.role] = $account
        }
    }
    return $accountMap
}

function Get-NextRunId {
    param([string]$Folder)
    $today = Get-Date -Format "yyyy-MM-dd"
    $maxSeq = 0
    if (Test-Path $Folder) {
        Get-ChildItem -Path $Folder -Filter "$today-auto-*.md" -File -ErrorAction SilentlyContinue | ForEach-Object {
            $match = [regex]::Match($_.BaseName, "^$today-auto-(\d+)$")
            if ($match.Success) {
                $seq = [int]$match.Groups[1].Value
                if ($seq -gt $maxSeq) {
                    $maxSeq = $seq
                }
            }
        }
    }
    return "$today-auto-$("{0:D3}" -f ($maxSeq + 1))"
}

function Assert-Branch {
    $branch = (git branch --show-current 2>$null)
    if (-not $branch) { throw "Unable to resolve current git branch" }
    if ($branch.Trim() -ne "git-ai/automation-route") {
        throw "Branch guard: current branch='$branch'. Please switch to git-ai/automation-route (or use the automation worktree) before running one-click regression."
    }
}

function Assert-UrlOk {
    param([string]$Name, [string]$Url)
    try {
        $resp = Invoke-WebRequest -Uri $Url -Method Get -TimeoutSec 8
        if ($resp.StatusCode -lt 200 -or $resp.StatusCode -ge 400) {
            throw "$Name not ok: HTTP $($resp.StatusCode)"
        }
    } catch {
        throw "$Name not reachable: $Url"
    }
}

function Assert-BackendHealthy {
    param([string]$Url)
    $resp = Invoke-RestMethod -Uri "$Url/api/system/init-status" -Method Get -TimeoutSec 8
    if ($resp.code -ne 200) {
        throw "Backend health failed: code=$($resp.code), msg=$($resp.msg)"
    }
}

if (-not $RunId) {
    $RunId = Get-NextRunId -Folder $runFolder
}
if (-not $RelatedTaskId) {
    $match = [regex]::Match($RunId, "auto-(\d+)$")
    if ($match.Success) {
        $RelatedTaskId = "AUTO-" + $match.Groups[1].Value
    } else {
        $RelatedTaskId = "AUTO-UNKNOWN"
    }
}

$status = @{
    branch_guard = "pending"
    availability = "pending"
    full_smoke   = "pending"
    role_paths   = "pending"
    overall      = "pending"
    error        = ""
}

$accountMap = Resolve-BusinessAccountMap -Path $AccountsFile
$athleteAccount = $accountMap["ATHLETE"]
$eventAdminAccount = $accountMap["EVENT_ADMIN"]
$userAccount = $accountMap["USER"]

$fullSmokeUsername = $Username
$fullSmokePassword = $Password
if ($athleteAccount) {
    $fullSmokeUsername = [string]$athleteAccount.username
    $fullSmokePassword = [string]$athleteAccount.password
}

Write-Host "=== LZ Sports One-Click Regression ==="
Write-Host "RunId   : $RunId"
Write-Host "Frontend: $FrontendUrl"
Write-Host "Backend : $BackendUrl"
Write-Host "EventId : $EventId"
Write-Host "User    : $fullSmokeUsername"
Write-Host "DryRun  : $DryRun"

try {
    Write-Step "Branch guard"
    Assert-Branch
    $status.branch_guard = "pass"

    Write-Step "Port/endpoint availability"
    if (-not $DryRun) {
        Assert-UrlOk -Name "Frontend" -Url $FrontendUrl
        Assert-BackendHealthy -Url $BackendUrl
        $status.availability = "pass"
    } else {
        Write-Step "DryRun: skipped availability checks"
        $status.availability = "skipped"
    }

    Write-Step "Run full-smoke (linkup + notification)"
    if (-not (Test-Path $fullSmoke)) { throw "Missing script: $fullSmoke" }
    if (-not $DryRun) {
        if ($athleteAccount -and $eventAdminAccount) {
            powershell -ExecutionPolicy Bypass -File $fullSmoke -FrontendUrl $FrontendUrl -BackendUrl $BackendUrl -Username $fullSmokeUsername -Password $fullSmokePassword -AdminUsername ([string]$eventAdminAccount.username) -AdminPassword ([string]$eventAdminAccount.password)
        } else {
            powershell -ExecutionPolicy Bypass -File $fullSmoke -FrontendUrl $FrontendUrl -BackendUrl $BackendUrl -Username $Username -Password $Password
        }
        if ($LASTEXITCODE -ne 0) { throw "full-smoke failed with exit code $LASTEXITCODE" }
        $status.full_smoke = "pass"
    } else {
        Write-Step "DryRun: skipped full-smoke"
        $status.full_smoke = "skipped"
    }

    Write-Step "Run role-path smoke (with EVENT_ADMIN binding)"
    if (-not (Test-Path $roleSmoke)) { throw "Missing script: $roleSmoke" }
    if (-not $DryRun) {
        if ($eventAdminAccount -and $userAccount -and $athleteAccount) {
            powershell -ExecutionPolicy Bypass -File $roleSmoke -BackendUrl $BackendUrl -EventId $EventId -EventAdminUsername ([string]$eventAdminAccount.username) -EventAdminPassword ([string]$eventAdminAccount.password) -UserUsername ([string]$userAccount.username) -UserPassword ([string]$userAccount.password) -AthleteUsername ([string]$athleteAccount.username) -AthletePassword ([string]$athleteAccount.password)
        } else {
            powershell -ExecutionPolicy Bypass -File $roleSmoke -BackendUrl $BackendUrl -EventId $EventId
        }
        if ($LASTEXITCODE -ne 0) { throw "role-path smoke failed with exit code $LASTEXITCODE" }
        $status.role_paths = "pass"
    } else {
        Write-Step "DryRun: skipped role-path smoke"
        $status.role_paths = "skipped"
    }

    $status.overall = "pass"
} catch {
    $status.overall = "fail"
    $status.error = $_.Exception.Message
    throw
} finally {
    Write-Step "Write run record"
    if (-not (Test-Path $runFolder)) {
        New-Item -ItemType Directory -Path $runFolder | Out-Null
    }

    $runPath = Join-Path $runFolder "$RunId.md"
    $escapedError = $status.error -replace "\r?\n", " "
    $signal = "none"
    if (-not [string]::IsNullOrWhiteSpace($escapedError)) {
        $signal = $escapedError
    }
    $content = @"
run_id: $RunId
related_task_id: $RelatedTaskId
changed_files:
  - scripts/smoke-oneclick.ps1
key_changes:
  - Added one-click regression entrypoint (branch guard + endpoint checks + full-smoke + rolepaths) with auto run record output
test_results:
  - overall: $($status.overall)
  - steps:
      branch_guard: $($status.branch_guard)
      availability: $($status.availability)
      full_smoke: $($status.full_smoke)
      role_paths: $($status.role_paths)
  - endpoints:
      frontend: "$FrontendUrl"
      backend: "$BackendUrl"
      eventId: $EventId
risks:
  - "One-click relies on existing local services and fixture accounts; backend env/DB/Redis must be ready."
issue_inputs:
  - source: local-run
    signal: "$signal"
root_causes:
  - none
fixes_applied:
  - none
token_source:
  - "tokens from /api/auth/login inside smoke scripts"
regression_watch:
  - "If fixture accounts change, update scripts/smoke-rolepaths.ps1 defaults."
repeated_failures:
  - issue_key: oneclick-regression
    attempts: 1
    blocked: false
next_action: "Run scripts/test.(bat|sh) oneclick to ensure the wrapper is stable."
"@

    Set-Content -Path $runPath -Value $content -Encoding UTF8
    Write-Step "Run record written: $runPath"
}

if ($status.overall -eq "pass") {
    Write-Host "=== One-Click Regression Completed Successfully ==="
}
