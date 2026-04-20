param(
    [string]$BackendUrl = "http://localhost:8080",
    [string]$FrontendUrl = "http://localhost:5173",
    [long]$EventId = 1,
    [string]$AccountsFile = "git-ai/automation-route/accounts/business-accounts-latest.json",
    [string]$BackendLog = "git-ai/automation-route/runs/backend-dev.log",
    [string]$RunId = "",
    [string]$RelatedTaskId = "",
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$runFolder = Join-Path $root "git-ai/automation-route/runs"
$oneclickScript = Join-Path $root "scripts/smoke-oneclick.ps1"
$workflowScript = Join-Path $root "scripts/smoke-event-workflow.ps1"
$registerScript = Join-Path $root "scripts/register-seed-users.ps1"
$normalizeEventScript = Join-Path $root "scripts/normalize-regression-event.ps1"

function Write-Step {
    param([string]$Msg)
    Write-Host "[SMOKE-SUITE] $Msg"
}

function Resolve-AbsolutePath {
    param([string]$Path)
    if ([System.IO.Path]::IsPathRooted($Path)) {
        return $Path
    }
    return (Join-Path $root $Path)
}

function Test-LoginAvailable {
    param([string]$Username, [string]$Password)
    try {
        $body = @{ username = $Username; password = $Password } | ConvertTo-Json
        $resp = Invoke-RestMethod -Uri "$BackendUrl/api/auth/login" -Method Post -ContentType "application/json" -Body $body -TimeoutSec 12
        return ($null -ne $resp -and [int]$resp.code -eq 200)
    } catch {
        return $false
    }
}

function Resolve-RegressionEventId {
    param([long]$PreferredEventId)
    try {
        $page = Invoke-RestMethod -Uri "$BackendUrl/api/event/page?currentPage=1&pageSize=10" -Method Get -TimeoutSec 12
        if ($null -eq $page -or [int]$page.code -ne 200 -or -not $page.data -or -not $page.data.records) {
            return $PreferredEventId
        }
        $records = @($page.data.records)
        if ($records.Count -eq 0) {
            return $PreferredEventId
        }
        return [long]$records[0].id
    } catch {
        return $PreferredEventId
    }
}

function Ensure-BusinessAccounts {
    param([string]$JsonPath, [string]$LogPath, [long]$TargetEventId)

    $needRefresh = $true
    if (Test-Path $JsonPath) {
        try {
            $json = Get-Content -Path $JsonPath -Raw | ConvertFrom-Json
            $roles = @{}
            foreach ($account in @($json.accounts)) {
                if ($account.role) {
                    $roles[[string]$account.role] = $account
                }
            }
            if ([long]$json.eventId -ne $TargetEventId) {
                $needRefresh = $true
            } elseif ($roles.ContainsKey("EVENT_ADMIN") -and $roles.ContainsKey("USER") -and $roles.ContainsKey("ATHLETE")) {
                $needRefresh =
                    (-not (Test-LoginAvailable -Username ([string]$roles["EVENT_ADMIN"].username) -Password ([string]$roles["EVENT_ADMIN"].password))) -or
                    (-not (Test-LoginAvailable -Username ([string]$roles["USER"].username) -Password ([string]$roles["USER"].password))) -or
                    (-not (Test-LoginAvailable -Username ([string]$roles["ATHLETE"].username) -Password ([string]$roles["ATHLETE"].password)))
            }
        } catch {
            $needRefresh = $true
        }
    }

    if (-not $needRefresh) {
        Write-Step "Business accounts asset is valid: $JsonPath"
        return
    }

    Write-Step "Business accounts asset is missing or stale, regenerate via register-seed-users"
    if (-not (Test-Path $registerScript)) {
        throw "Missing script: $registerScript"
    }

    $markdownPath = [System.IO.Path]::ChangeExtension($JsonPath, ".md")
    $registerArgs = @(
        "-ExecutionPolicy", "Bypass",
        "-File", $registerScript,
        "-BackendUrl", $BackendUrl,
        "-BackendLog", $LogPath,
        "-EventId", $TargetEventId,
        "-OutJson", $JsonPath,
        "-OutMarkdown", $markdownPath
    )
    powershell @registerArgs
    if ($LASTEXITCODE -ne 0) {
        throw "register-seed-users failed with exit code $LASTEXITCODE"
    }
}

function Resolve-WorkflowEventIdFromAccounts {
    param([string]$JsonPath, [long]$FallbackEventId)
    try {
        $json = Get-Content -Path $JsonPath -Raw | ConvertFrom-Json
        $athlete = @($json.accounts | Where-Object { $_.role -eq "ATHLETE" } | Select-Object -First 1)
        if (-not $athlete) {
            return $FallbackEventId
        }
        $body = @{ username = [string]$athlete.username; password = [string]$athlete.password } | ConvertTo-Json
        $login = Invoke-RestMethod -Uri "$BackendUrl/api/auth/login" -Method Post -ContentType "application/json" -Body $body -TimeoutSec 12
        if ($null -eq $login -or [int]$login.code -ne 200 -or -not $login.data -or -not $login.data.token) {
            return $FallbackEventId
        }
        $headers = @{ Authorization = "Bearer $($login.data.token)" }
        $page = Invoke-RestMethod -Uri "$BackendUrl/api/event/page?currentPage=1&pageSize=10" -Method Get -Headers $headers -TimeoutSec 12
        if ($null -eq $page -or [int]$page.code -ne 200 -or -not $page.data -or -not $page.data.records) {
            return $FallbackEventId
        }
        foreach ($record in @($page.data.records)) {
            $candidateId = [long]$record.id
            $eventResp = Invoke-RestMethod -Uri "$BackendUrl/api/event/$candidateId" -Method Get -Headers $headers -TimeoutSec 12
            $projectResp = Invoke-RestMethod -Uri "$BackendUrl/api/project/event/$candidateId" -Method Get -Headers $headers -TimeoutSec 12
            if ([int]$eventResp.code -eq 200 -and [int]$projectResp.code -eq 200 -and $projectResp.data -and @($projectResp.data).Count -gt 0) {
                return $candidateId
            }
        }
        return $FallbackEventId
    } catch {
        return $FallbackEventId
    }
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

$oneclickRunId = "$RunId-oneclick"
$workflowRunId = "$RunId-workflow"

$status = [ordered]@{
    oneclick = "pending"
    event_workflow = "pending"
    overall = "pending"
    error = ""
}

Write-Host "=== LZ Sports Smoke Suite ==="
Write-Host "RunId       : $RunId"
Write-Host "BackendUrl  : $BackendUrl"
Write-Host "FrontendUrl : $FrontendUrl"
Write-Host "EventId     : $EventId"
Write-Host "AccountsFile: $AccountsFile"
Write-Host "BackendLog  : $BackendLog"
Write-Host "DryRun      : $DryRun"

try {
    if (-not (Test-Path $oneclickScript)) { throw "Missing script: $oneclickScript" }
    if (-not (Test-Path $workflowScript)) { throw "Missing script: $workflowScript" }
    if (-not (Test-Path $normalizeEventScript)) { throw "Missing script: $normalizeEventScript" }
    $accountsFileAbs = Resolve-AbsolutePath -Path $AccountsFile
    $backendLogAbs = Resolve-AbsolutePath -Path $BackendLog
    $effectiveEventId = Resolve-RegressionEventId -PreferredEventId $EventId
    Write-Step "Regression event id: $effectiveEventId"

    if (-not $DryRun) {
        powershell -ExecutionPolicy Bypass -File $normalizeEventScript -EventId $effectiveEventId
        if ($LASTEXITCODE -ne 0) {
            throw "normalize regression event failed with exit code $LASTEXITCODE"
        }
        Ensure-BusinessAccounts -JsonPath $accountsFileAbs -LogPath $backendLogAbs -TargetEventId $effectiveEventId
        $workflowEventId = Resolve-WorkflowEventIdFromAccounts -JsonPath $accountsFileAbs -FallbackEventId $effectiveEventId
        if ($workflowEventId -ne $effectiveEventId) {
            Write-Step "Adjust regression event id from $effectiveEventId to athlete-visible event $workflowEventId"
            $effectiveEventId = $workflowEventId
            powershell -ExecutionPolicy Bypass -File $normalizeEventScript -EventId $effectiveEventId
            if ($LASTEXITCODE -ne 0) {
                throw "normalize regression event failed with exit code $LASTEXITCODE"
            }
            Ensure-BusinessAccounts -JsonPath $accountsFileAbs -LogPath $backendLogAbs -TargetEventId $effectiveEventId
        }
    }

    Write-Step "Run oneclick baseline regression"
    $oneclickArgs = @(
        "-ExecutionPolicy", "Bypass",
        "-File", $oneclickScript,
        "-BackendUrl", $BackendUrl,
        "-FrontendUrl", $FrontendUrl,
        "-EventId", $effectiveEventId,
        "-AccountsFile", $accountsFileAbs,
        "-RunId", $oneclickRunId,
        "-RelatedTaskId", $RelatedTaskId
    )
    if ($DryRun) { $oneclickArgs += "-DryRun" }
    powershell @oneclickArgs
    if ($LASTEXITCODE -ne 0) {
        throw "oneclick suite step failed with exit code $LASTEXITCODE"
    }
    $status.oneclick = "pass"

    Write-Step "Run current-event workflow regression"
    $workflowArgs = @(
        "-ExecutionPolicy", "Bypass",
        "-File", $workflowScript,
        "-BackendUrl", $BackendUrl,
        "-EventId", $effectiveEventId,
        "-AccountsFile", $accountsFileAbs,
        "-RunId", $workflowRunId,
        "-RelatedTaskId", $RelatedTaskId
    )
    if ($DryRun) { $workflowArgs += "-DryRun" }
    powershell @workflowArgs
    if ($LASTEXITCODE -ne 0) {
        throw "event workflow suite step failed with exit code $LASTEXITCODE"
    }
    $status.event_workflow = "pass"

    $status.overall = "pass"
} catch {
    $status.overall = "fail"
    $status.error = $_.Exception.Message
    throw
} finally {
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
  - scripts/smoke-suite.ps1
  - git-ai/automation-route/runs/$RunId.md
key_changes:
  - Added a single entrypoint to run the baseline oneclick regression and the current-event full workflow regression in one pass.
  - Wired the suite to reuse the latest generated business role asset file for workflow replay.
test_results:
  - overall: $($status.overall)
  - steps:
      oneclick: $($status.oneclick)
      event_workflow: $($status.event_workflow)
  - sub_runs:
      oneclick: "$oneclickRunId"
      event_workflow: "$workflowRunId"
risks:
  - "The suite mutates local event data because the event-workflow step creates or refreshes one registration and approves it."
  - "The event-workflow step depends on a valid business-accounts-latest.json asset file."
issue_inputs:
  - source: local-run
    signal: "$escapedError"
repeated_failures:
  - issue_key: smoke-suite
    attempts: 1
    blocked: false
next_action: "Use scripts/test.(bat|sh) suite for the combined baseline + full event workflow regression."
"@

    Set-Content -Path $runPath -Value $content -Encoding UTF8
    Write-Step "Run record written: $runPath"
}

if ($status.overall -eq "pass") {
    Write-Host "=== Smoke Suite Completed Successfully ==="
}
