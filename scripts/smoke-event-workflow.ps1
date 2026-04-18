param(
    [string]$BackendUrl = "http://localhost:8080",
    [long]$EventId = 1,
    [string]$AccountsFile = "git-ai/automation-route/runs/business-accounts-latest.json",
    [string]$RunId = "",
    [string]$RelatedTaskId = "AUTO-034",
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$runFolder = Join-Path $root "git-ai/automation-route/runs"

function Write-Step {
    param([string]$Msg)
    Write-Host "[EVENT-WORKFLOW] $Msg"
}

function Resolve-Token {
    param([object]$RespData)
    if ($null -eq $RespData) { return $null }
    if ($RespData -is [string]) { return $RespData }
    if ($RespData.PSObject.Properties.Name -contains "token") { return $RespData.token }
    return $null
}

function Assert-Code {
    param([string]$Name, [object]$Resp, [int[]]$Allowed)
    if ($null -eq $Resp) { throw "$Name failed: empty response" }
    if (-not ($Allowed -contains [int]$Resp.code)) {
        throw "$Name failed: expected [$($Allowed -join ',')], got code=$($Resp.code), msg=$($Resp.msg)"
    }
}

function Login-Account {
    param([string]$Username, [string]$Password)
    $body = @{ username = $Username; password = $Password } | ConvertTo-Json
    $resp = Invoke-RestMethod -Uri "$BackendUrl/api/auth/login" -Method Post -ContentType "application/json" -Body $body -TimeoutSec 12
    Assert-Code -Name "login($Username)" -Resp $resp -Allowed @(200)
    $token = Resolve-Token -RespData $resp.data
    if (-not $token) { throw "login($Username) failed: missing token" }
    return [PSCustomObject]@{
        id = [long]$resp.data.id
        username = $Username
        role = [string]$resp.data.role
        token = $token
    }
}

function Api-Get {
    param([string]$Path, [hashtable]$Headers)
    return Invoke-RestMethod -Uri "$BackendUrl$Path" -Method Get -Headers $Headers -TimeoutSec 12
}

function Api-Post {
    param([string]$Path, [hashtable]$Headers, [object]$Body)
    if ($null -eq $Body) {
        return Invoke-RestMethod -Uri "$BackendUrl$Path" -Method Post -Headers $Headers -TimeoutSec 12
    }
    $json = $Body | ConvertTo-Json -Depth 10
    return Invoke-RestMethod -Uri "$BackendUrl$Path" -Method Post -Headers $Headers -ContentType "application/json" -Body $json -TimeoutSec 12
}

function Api-Put {
    param([string]$Path, [hashtable]$Headers, [object]$Body)
    if ($null -eq $Body) {
        return Invoke-RestMethod -Uri "$BackendUrl$Path" -Method Put -Headers $Headers -TimeoutSec 12
    }
    $json = $Body | ConvertTo-Json -Depth 10
    return Invoke-RestMethod -Uri "$BackendUrl$Path" -Method Put -Headers $Headers -ContentType "application/json" -Body $json -TimeoutSec 12
}

function Api-Delete {
    param([string]$Path, [hashtable]$Headers)
    return Invoke-RestMethod -Uri "$BackendUrl$Path" -Method Delete -Headers $Headers -TimeoutSec 12
}

function Get-NotificationState {
    param([hashtable]$Headers, [string]$Name)
    $unread = Api-Get -Path "/api/notification/unread-count" -Headers $Headers
    Assert-Code -Name "$Name-unread" -Resp $unread -Allowed @(200)
    $page = Api-Get -Path "/api/notification/page?currentPage=1&pageSize=20&isRead=false" -Headers $Headers
    Assert-Code -Name "$Name-page" -Resp $page -Allowed @(200)
    $records = @()
    if ($page.data -and $page.data.records) {
        $records = @($page.data.records)
    }
    return [PSCustomObject]@{
        unread = [int]$unread.data
        records = $records
    }
}

function Wait-ForNotificationGrowth {
    param(
        [hashtable]$Headers,
        [int]$UnreadBaseline,
        [string]$Name
    )
    $current = $null
    for ($i = 0; $i -lt 10; $i++) {
        Start-Sleep -Milliseconds 500
        $current = Get-NotificationState -Headers $Headers -Name $Name
        if ($current.unread -ge $UnreadBaseline -and $current.records.Count -gt 0) {
            return $current
        }
    }
    if ($null -eq $current) {
        throw "$Name failed: notification state unavailable"
    }
    if ($current.unread -lt $UnreadBaseline) {
        throw "$Name failed: unread count dropped unexpectedly"
    }
    if ($current.records.Count -eq 0) {
        throw "$Name failed: missing unread notifications"
    }
    return $current
}

function Invoke-ApplyWithRetry {
    param(
        [long]$ProjectId,
        [hashtable]$Headers,
        [string]$Name,
        [int]$MaxAttempts = 6
    )
    $lastResp = $null
    for ($attempt = 1; $attempt -le $MaxAttempts; $attempt++) {
        $lastResp = Api-Post -Path "/api/registration/apply/$ProjectId" -Headers $Headers -Body $null
        if ([int]$lastResp.code -eq 200) {
            return $lastResp
        }
        if ([int]$lastResp.code -ne 409) {
            break
        }
        Start-Sleep -Seconds 1
    }
    Assert-Code -Name $Name -Resp $lastResp -Allowed @(200)
    return $lastResp
}

if (-not $RunId) {
    $RunId = (Get-Date -Format "yyyy-MM-dd") + "-auto-040"
}

$status = [ordered]@{
    oneclick = "pending"
    event_detail = "pending"
    project_list = "pending"
    athlete_status = "pending"
    user_negative_apply = "pending"
    athlete_apply = "pending"
    admin_reject = "pending"
    reject_notification = "pending"
    reject_registration_page = "pending"
    athlete_reapply = "pending"
    admin_audit = "pending"
    approve_notification = "pending"
    registration_page = "pending"
    score_upsert = "pending"
    score_publish = "pending"
    score_checks = "pending"
    overall = "pending"
    error = ""
}

Write-Host "=== LZ Sports Event Workflow Smoke ==="
Write-Host "RunId       : $RunId"
Write-Host "BackendUrl  : $BackendUrl"
Write-Host "EventId     : $EventId"
Write-Host "AccountsFile: $AccountsFile"
Write-Host "DryRun      : $DryRun"

try {
    if (-not (Test-Path $AccountsFile)) {
        throw "accounts file missing: $AccountsFile"
    }
    $accounts = Get-Content $AccountsFile -Raw | ConvertFrom-Json
    $eventAdminSeed = $accounts.accounts | Where-Object { $_.role -eq "EVENT_ADMIN" } | Select-Object -First 1
    $userSeed = $accounts.accounts | Where-Object { $_.role -eq "USER" } | Select-Object -First 1
    $athleteSeed = $accounts.accounts | Where-Object { $_.role -eq "ATHLETE" } | Select-Object -First 1
    if (-not $eventAdminSeed -or -not $userSeed -or -not $athleteSeed) {
        throw "accounts file does not contain EVENT_ADMIN / USER / ATHLETE"
    }

    Write-Step "Login latest role assets"
    $eventAdmin = Login-Account -Username $eventAdminSeed.username -Password $eventAdminSeed.password
    $user = Login-Account -Username $userSeed.username -Password $userSeed.password
    $athlete = Login-Account -Username $athleteSeed.username -Password $athleteSeed.password
    $hEvent = @{ Authorization = "Bearer $($eventAdmin.token)" }
    $hUser = @{ Authorization = "Bearer $($user.token)" }
    $hAthlete = @{ Authorization = "Bearer $($athlete.token)" }

    Write-Step "Current event detail"
    $eventResp = Api-Get -Path "/api/event/$EventId" -Headers $hAthlete
    Assert-Code -Name "event-detail" -Resp $eventResp -Allowed @(200)
    $status.event_detail = "pass"

    Write-Step "Event project list"
    $projectResp = Api-Get -Path "/api/project/event/$EventId" -Headers $hAthlete
    Assert-Code -Name "project-list" -Resp $projectResp -Allowed @(200)
    if (-not $projectResp.data -or $projectResp.data.Count -eq 0) {
        throw "project-list failed: no project under eventId=$EventId"
    }
    $projects = @($projectResp.data)
    $status.project_list = "pass"

    Write-Step "Athlete qualification status"
    $athleteStatusResp = Api-Get -Path "/api/athlete/apply/$($athlete.id)?eventId=$EventId" -Headers $hAthlete
    Assert-Code -Name "athlete-apply-status" -Resp $athleteStatusResp -Allowed @(200)
    if (-not $athleteStatusResp.data -or $athleteStatusResp.data.athleteState -ne "APPROVED") {
        throw "athlete qualification not approved for eventId=$EventId"
    }
    $status.athlete_status = "pass"

    Write-Step "Athlete unread count before audit"
    $notificationBefore = Get-NotificationState -Headers $hAthlete -Name "notification-before"

    Write-Step "Athlete registration page before apply"
    $regBefore = Api-Get -Path "/api/registration/page?currentPage=1&pageSize=100" -Headers $hAthlete
    Assert-Code -Name "registration-page-before" -Resp $regBefore -Allowed @(200)
    $recordsBefore = @()
    if ($regBefore.data -and $regBefore.data.records) { $recordsBefore = @($regBefore.data.records) }

    $activeStatuses = @("PENDING", "APPROVED", "CONFIRMED")
    $activeItemIds = @($recordsBefore | Where-Object { $activeStatuses -contains $_.registrationStatus } | ForEach-Object { [long]$_.itemId })
    $targetProject = @($projects | Where-Object { $activeItemIds -notcontains [long]$_.id } | Select-Object -First 1)

    $needsFreeSlot = (-not $targetProject) -or (@($recordsBefore | Where-Object { $activeStatuses -contains $_.registrationStatus }).Count -ge 3)
    if ($needsFreeSlot) {
        $existing = @($recordsBefore | Where-Object { $activeStatuses -contains $_.registrationStatus } | Select-Object -First 1)
        if (-not $existing) {
            throw "unable to find reusable or cancellable project for athlete"
        }
        Write-Step "Cancel existing active registration to free a project"
        $cancelResp = Api-Delete -Path "/api/registration/$($existing.id)" -Headers $hAthlete
        Assert-Code -Name "registration-cancel-existing" -Resp $cancelResp -Allowed @(200)
        if (-not $targetProject) {
            $targetProject = @($projects | Where-Object { [long]$_.id -eq [long]$existing.itemId } | Select-Object -First 1)
        }
    }

    if (-not $targetProject) {
        throw "unable to resolve target project"
    }

    Write-Step "USER negative apply should be rejected"
    $userApply = Api-Post -Path "/api/registration/apply/$($targetProject.id)" -Headers $hUser -Body $null
    Assert-Code -Name "user-negative-apply" -Resp $userApply -Allowed @(409)
    $status.user_negative_apply = "pass"

    Write-Step "ATHLETE apply project"
    $applyResp = Api-Post -Path "/api/registration/apply/$($targetProject.id)" -Headers $hAthlete -Body $null
    Assert-Code -Name "athlete-apply" -Resp $applyResp -Allowed @(200)
    $status.athlete_apply = "pass"

    Write-Step "EVENT_ADMIN query pending registrations and reject"
    $adminPage = Api-Get -Path "/api/registration/page?currentPage=1&pageSize=100&status=PENDING" -Headers $hEvent
    Assert-Code -Name "admin-registration-page" -Resp $adminPage -Allowed @(200)
    $pendingRecords = @()
    if ($adminPage.data -and $adminPage.data.records) { $pendingRecords = @($adminPage.data.records) }
    $targetReg = @($pendingRecords | Where-Object { [long]$_.athleteId -eq [long]$athlete.id -and [long]$_.itemId -eq [long]$targetProject.id } | Select-Object -First 1)
    if (-not $targetReg) {
        throw "cannot find pending registration for athlete=$($athlete.username), projectId=$($targetProject.id)"
    }
    $rejectResp = Api-Put -Path "/api/registration/refuse/$($targetReg.id)?eventId=$EventId" -Headers $hEvent -Body $null
    Assert-Code -Name "registration-refuse" -Resp $rejectResp -Allowed @(200)
    $status.admin_reject = "pass"

    Write-Step "Athlete notification check after reject"
    $rejectNotification = Wait-ForNotificationGrowth -Headers $hAthlete -UnreadBaseline $notificationBefore.unread -Name "notification-after-reject"
    $status.reject_notification = "pass"

    Write-Step "Athlete registration page after reject"
    $regAfterReject = Api-Get -Path "/api/registration/page?currentPage=1&pageSize=100" -Headers $hAthlete
    Assert-Code -Name "registration-page-after-reject" -Resp $regAfterReject -Allowed @(200)
    $rejectRecords = @()
    if ($regAfterReject.data -and $regAfterReject.data.records) { $rejectRecords = @($regAfterReject.data.records) }
    $rejectedRecord = @($rejectRecords | Where-Object { [long]$_.itemId -eq [long]$targetProject.id -and $_.registrationStatus -eq "REJECTED" } | Select-Object -First 1)
    if (-not $rejectedRecord) {
        throw "athlete registration page missing REJECTED record for projectId=$($targetProject.id)"
    }
    $status.reject_registration_page = "pass"

    Write-Step "ATHLETE reapply project after reject"
    $reapplyResp = Invoke-ApplyWithRetry -ProjectId ([long]$targetProject.id) -Headers $hAthlete -Name "athlete-reapply"
    $status.athlete_reapply = "pass"

    Write-Step "EVENT_ADMIN query pending registrations and approve"
    $adminPage = Api-Get -Path "/api/registration/page?currentPage=1&pageSize=100&status=PENDING" -Headers $hEvent
    Assert-Code -Name "admin-registration-page-reapply" -Resp $adminPage -Allowed @(200)
    $pendingRecords = @()
    if ($adminPage.data -and $adminPage.data.records) { $pendingRecords = @($adminPage.data.records) }
    $targetReg = @($pendingRecords | Where-Object { [long]$_.athleteId -eq [long]$athlete.id -and [long]$_.itemId -eq [long]$targetProject.id } | Select-Object -First 1)
    if (-not $targetReg) {
        throw "cannot find re-applied pending registration for athlete=$($athlete.username), projectId=$($targetProject.id)"
    }
    $approveResp = Api-Put -Path "/api/registration/attend/$($targetReg.id)?eventId=$EventId" -Headers $hEvent -Body $null
    Assert-Code -Name "registration-attend" -Resp $approveResp -Allowed @(200)
    $status.admin_audit = "pass"

    Write-Step "Athlete notification check after approve"
    $approveNotification = Wait-ForNotificationGrowth -Headers $hAthlete -UnreadBaseline $rejectNotification.unread -Name "notification-after-approve"
    $status.approve_notification = "pass"

    Write-Step "Athlete registration page after approve"
    $regAfter = Api-Get -Path "/api/registration/page?currentPage=1&pageSize=100" -Headers $hAthlete
    Assert-Code -Name "registration-page-after" -Resp $regAfter -Allowed @(200)
    $afterRecords = @()
    if ($regAfter.data -and $regAfter.data.records) { $afterRecords = @($regAfter.data.records) }
    $approvedRecord = @($afterRecords | Where-Object { [long]$_.itemId -eq [long]$targetProject.id -and $_.registrationStatus -eq "APPROVED" } | Select-Object -First 1)
    if (-not $approvedRecord) {
        throw "athlete registration page missing APPROVED record for projectId=$($targetProject.id)"
    }
    $status.registration_page = "pass"

    $myScores = $null
    $publicScores = $null
    $mine = $null

    Write-Step "Check whether the approved registration already has a published score"
    $myScoresBeforeUpsert = Api-Get -Path "/api/score/my?eventId=$EventId" -Headers $hAthlete
    Assert-Code -Name "score-my-before-upsert" -Resp $myScoresBeforeUpsert -Allowed @(200)
    $myListBeforeUpsert = @()
    if ($myScoresBeforeUpsert.data) { $myListBeforeUpsert = @($myScoresBeforeUpsert.data) }
    $existingPublishedScore = @($myListBeforeUpsert | Where-Object { [long]$_.registrationId -eq [long]$targetReg.id } | Select-Object -First 1)

    if ($existingPublishedScore) {
        Write-Step "Approved registration already has published score, skip upsert/publish and verify queries"
        $status.score_upsert = "pass"
        $status.score_publish = "pass"
        $myScores = $myScoresBeforeUpsert
        $mine = $existingPublishedScore
    } else {
        Write-Step "Upsert one score for the approved registration"
        $scoreBody = @{
            registrationId = [long]$targetReg.id
            scoreValue = "12.34"
            scoreRank = 1
            remark = "auto-smoke"
        }
        $upsert = Api-Post -Path "/api/score/upsert" -Headers $hEvent -Body $scoreBody
        Assert-Code -Name "score-upsert" -Resp $upsert -Allowed @(200)
        $status.score_upsert = "pass"

        Write-Step "Publish scores for event"
        $publish = Api-Put -Path "/api/score/publish/$EventId" -Headers $hEvent -Body $null
        Assert-Code -Name "score-publish" -Resp $publish -Allowed @(200)
        $status.score_publish = "pass"

        $myScores = Api-Get -Path "/api/score/my?eventId=$EventId" -Headers $hAthlete
        Assert-Code -Name "score-my" -Resp $myScores -Allowed @(200)
        $myList = @()
        if ($myScores.data) { $myList = @($myScores.data) }
        $mine = @($myList | Where-Object { [long]$_.registrationId -eq [long]$targetReg.id } | Select-Object -First 1)
    }

    Write-Step "Score and public ranking checks"
    if ($null -eq $myScores) {
        $myScores = Api-Get -Path "/api/score/my?eventId=$EventId" -Headers $hAthlete
        Assert-Code -Name "score-my" -Resp $myScores -Allowed @(200)
    }
    $publicScores = Api-Get -Path "/api/score/public/$EventId" -Headers $hAthlete
    Assert-Code -Name "score-public" -Resp $publicScores -Allowed @(200)
    if (-not $mine) {
        throw "score-my missing published score for registrationId=$($targetReg.id)"
    }
    $publicRows = @()
    if ($publicScores.data) {
        foreach ($group in $publicScores.data.PSObject.Properties) {
            if ($group.Value) {
                $publicRows += @($group.Value)
            }
        }
    }
    $publicMine = @($publicRows | Where-Object { [long]$_.registrationId -eq [long]$targetReg.id } | Select-Object -First 1)
    if (-not $publicMine) {
        throw "score-public missing published score for registrationId=$($targetReg.id)"
    }
    $stats = Api-Get -Path "/api/event-admin/$EventId/registrations/stats" -Headers $hEvent
    Assert-Code -Name "registration-stats" -Resp $stats -Allowed @(200)
    $status.score_checks = "pass"

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
  - scripts/smoke-event-workflow.ps1
  - git-ai/automation-route/runs/$RunId.md
key_changes:
  - Added replayable current-event workflow smoke using the latest generated EVENT_ADMIN / USER / ATHLETE asset accounts.
  - The flow verifies event detail, project list, athlete qualification, user negative apply, athlete apply, event-admin reject, rejection notification/status, same-project reapply, event-admin approve, approval notification, registration status, and score-related queries.
test_results:
  - overall: $($status.overall)
  - steps:
      event_detail: $($status.event_detail)
      project_list: $($status.project_list)
      athlete_status: $($status.athlete_status)
      user_negative_apply: $($status.user_negative_apply)
      athlete_apply: $($status.athlete_apply)
      admin_reject: $($status.admin_reject)
      reject_notification: $($status.reject_notification)
      reject_registration_page: $($status.reject_registration_page)
      athlete_reapply: $($status.athlete_reapply)
      admin_audit: $($status.admin_audit)
      approve_notification: $($status.approve_notification)
      registration_page: $($status.registration_page)
      score_upsert: $($status.score_upsert)
      score_publish: $($status.score_publish)
      score_checks: $($status.score_checks)
risks:
  - "Workflow replay mutates current local event data by creating one rejected registration, then re-applying and approving one registration."
  - "The flow depends on the freshness of business-accounts-latest.json and the current event being OPEN within the registration window."
issue_inputs:
  - source: local-run
    signal: "$escapedError"
token_source:
  - "fresh /api/auth/login tokens from business-accounts-latest.json usernames/passwords"
repeated_failures:
  - issue_key: current-event-full-workflow
    attempts: 1
    blocked: false
next_action: "Reuse this workflow smoke after any event, registration, or notification change."
"@
    Set-Content -Path $runPath -Value $content -Encoding UTF8
    Write-Step "Run record written: $runPath"
}

if ($status.overall -eq "pass") {
    Write-Host "=== Event Workflow Smoke Completed Successfully ==="
}
