param(
    [string]$BackendUrl = "http://localhost:8080",
    [string]$AccountsFile = "git-ai/automation-route/runs/business-accounts-latest.json",
    [string]$SchoolAdminUsername = "init_school_admin_01",
    [string]$SchoolAdminPassword = "Admin12345",
    [string]$RunId = "",
    [string]$RelatedTaskId = "AUTO-045",
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$runFolder = Join-Path $root "git-ai/automation-route/runs"

function Write-Step {
    param([string]$Msg)
    Write-Host "[SCORE-MANAGE] $Msg"
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

function Resolve-AbsolutePath {
    param([string]$Path)
    if ([System.IO.Path]::IsPathRooted($Path)) {
        return $Path
    }
    return Join-Path $root $Path
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

function Resolve-SchoolAdminLogin {
    $candidates = @(
        @{ username = $SchoolAdminUsername; password = $SchoolAdminPassword },
        @{ username = $SchoolAdminUsername; password = "admin123" },
        @{ username = "school_admin_96719547"; password = "admin123" },
        @{ username = "school_admin_62e2507a"; password = "admin123" },
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

    throw "Unable to resolve a working SCHOOL_ADMIN account."
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
    if ($Body -is [System.Array] -and $Body.Length -eq 1) {
        $json = "[" + $Body[0].ToString() + "]"
    } else {
        $json = $Body | ConvertTo-Json -Depth 10
    }
    return Invoke-RestMethod -Uri "$BackendUrl$Path" -Method Post -Headers $Headers -ContentType "application/json" -Body $json -TimeoutSec 12
}

function Api-Put {
    param([string]$Path, [hashtable]$Headers, [object]$Body)
    if ($null -eq $Body) {
        return Invoke-RestMethod -Uri "$BackendUrl$Path" -Method Put -Headers $Headers -ContentType "application/json" -Body "{}" -TimeoutSec 12
    }
    $json = $Body | ConvertTo-Json -Depth 10
    return Invoke-RestMethod -Uri "$BackendUrl$Path" -Method Put -Headers $Headers -ContentType "application/json" -Body $json -TimeoutSec 12
}

function New-DateString {
    param([datetime]$Value)
    return $Value.ToString("yyyy-MM-dd HH:mm:ss")
}

function Ensure-Profile {
    param([hashtable]$Headers, [string]$Username)
    $infoResp = Api-Get -Path "/api/auth/info" -Headers $Headers
    Assert-Code -Name "user-info" -Resp $infoResp -Allowed @(200)
    $profile = $infoResp.data
    $needsProfileUpdate = (
        -not $profile -or
        [string]::IsNullOrWhiteSpace([string]$profile.name) -or
        [string]::IsNullOrWhiteSpace([string]$profile.gender) -or
        [string]::IsNullOrWhiteSpace([string]$profile.contact) -or
        $null -eq $profile.deptId -or
        [long]$profile.deptId -eq 0
    )
    if (-not $needsProfileUpdate) { return }

    $suffix = Get-Date -Format "HHmmss"
    $profileBody = @{
        userName = $Username
        name = "录分用户$suffix"
        gender = [string]([char]0x7537)
        contact = "1380000$suffix"
        deptId = 11
    }
    $updateResp = Api-Post -Path "/api/auth/update" -Headers $Headers -Body $profileBody
    Assert-Code -Name "update-user-profile" -Resp $updateResp -Allowed @(200, 409)
}

function Wait-ForAthleteApplication {
    param([hashtable]$Headers, [long]$EventId, [long]$UserId)
    for ($i = 0; $i -lt 10; $i++) {
        Start-Sleep -Milliseconds 500
        $resp = Api-Get -Path "/api/event-admin/$EventId/athlete-applications?status=PENDING&page=1&size=100" -Headers $Headers
        Assert-Code -Name "athlete-applications-pending" -Resp $resp -Allowed @(200)
        $records = @()
        if ($resp.data -and $resp.data.records) { $records = @($resp.data.records) }
        $record = @($records | Where-Object { [long]$_.userId -eq [long]$UserId } | Select-Object -First 1)
        if ($record) { return $record }
    }
    throw "cannot find pending athlete application for userId=$UserId"
}

function Wait-ForRegistration {
    param([hashtable]$Headers, [long]$AthleteId, [long]$ProjectId)
    for ($i = 0; $i -lt 10; $i++) {
        Start-Sleep -Milliseconds 500
        $resp = Api-Get -Path "/api/registration/page?currentPage=1&pageSize=200&status=PENDING" -Headers $Headers
        Assert-Code -Name "registration-page-pending" -Resp $resp -Allowed @(200)
        $records = @()
        if ($resp.data -and $resp.data.records) { $records = @($resp.data.records) }
        $record = @($records | Where-Object { [long]$_.athleteId -eq [long]$AthleteId -and [long]$_.itemId -eq [long]$ProjectId } | Select-Object -First 1)
        if ($record) { return $record }
    }
    throw "cannot find pending registration for athleteId=$AthleteId projectId=$ProjectId"
}

function Expect-BusinessErrorCode {
    param(
        [scriptblock]$Action,
        [int]$ExpectedCode,
        [string]$Name
    )
    try {
        $resp = & $Action
        if ($null -ne $resp -and $resp.PSObject.Properties.Name -contains "code") {
            if ([int]$resp.code -eq $ExpectedCode) {
                return $resp
            }
            throw "$Name failed: expected business code=$ExpectedCode, got code=$($resp.code), msg=$($resp.msg)"
        }
    } catch {
        $response = $_.Exception.Response
        if ($response) {
            $statusCode = [int]$response.StatusCode.value__
            $reader = New-Object System.IO.StreamReader($response.GetResponseStream())
            $body = $reader.ReadToEnd()
            $reader.Close()
            $parsed = $body | ConvertFrom-Json
            if ([int]$parsed.code -ne $ExpectedCode) {
                throw "$Name failed: expected business code=$ExpectedCode, got code=$($parsed.code), http=$statusCode, msg=$($parsed.msg)"
            }
            return $parsed
        }
        throw
    }
    throw "$Name failed: expected business code=$ExpectedCode but call succeeded"
}

if (-not $RunId) {
    $RunId = (Get-Date -Format "yyyy-MM-dd") + "-auto-045-score-manage"
}

$status = [ordered]@{
    school_admin_login = "pending"
    event_admin_login = "pending"
    user_login = "pending"
    create_assigned_event = "pending"
    create_control_event = "pending"
    bind_event_admin = "pending"
    publish_events = "pending"
    event_scope = "pending"
    unauthorized_candidates = "pending"
    athlete_apply = "pending"
    athlete_approve = "pending"
    registration_apply = "pending"
    registration_approve = "pending"
    score_candidates_before_save = "pending"
    batch_save = "pending"
    score_candidates_after_save = "pending"
    score_publish = "pending"
    score_lock = "pending"
    score_queries = "pending"
    overall = "pending"
    error = ""
}

$assignedEventId = $null
$controlEventId = $null
$assignedEventName = $null
$controlEventName = $null
$projectIds = @()
$registrationIds = @()

Write-Host "=== LZ Sports Score Manage Smoke ==="
Write-Host "RunId       : $RunId"
Write-Host "BackendUrl  : $BackendUrl"
Write-Host "AccountsFile: $AccountsFile"
Write-Host "DryRun      : $DryRun"

try {
    $accountsPath = Resolve-AbsolutePath -Path $AccountsFile
    if (-not (Test-Path $accountsPath)) {
        throw "accounts file missing: $accountsPath"
    }
    $accounts = Get-Content -Path $accountsPath -Raw | ConvertFrom-Json
    $eventAdminSeed = @($accounts.accounts | Where-Object { $_.role -eq "EVENT_ADMIN" } | Select-Object -First 1)
    $userSeed = @($accounts.accounts | Where-Object { $_.role -eq "USER" } | Select-Object -First 1)
    if (-not $eventAdminSeed -or -not $userSeed) {
        throw "accounts file does not contain EVENT_ADMIN / USER"
    }

    Write-Step "Resolve SCHOOL_ADMIN and business accounts"
    $schoolAdmin = Resolve-SchoolAdminLogin
    $eventAdmin = Login-Account -Username ([string]$eventAdminSeed.username) -Password ([string]$eventAdminSeed.password)
    $user = Login-Account -Username ([string]$userSeed.username) -Password ([string]$userSeed.password)
    $hSchool = @{ Authorization = "Bearer $($schoolAdmin.token)" }
    $hEvent = @{ Authorization = "Bearer $($eventAdmin.token)" }
    $hUser = @{ Authorization = "Bearer $($user.token)" }
    $status.school_admin_login = "pass"
    $status.event_admin_login = "pass"
    $status.user_login = "pass"

    Ensure-Profile -Headers $hUser -Username ([string]$userSeed.username)

    $now = Get-Date
    $assignedEventName = "AUTO-045 Score Assigned " + (Get-Date -Format "HHmmss")
    $controlEventName = "AUTO-045 Score Control " + (Get-Date -Format "HHmmss")

    $assignedEventBody = @{
        name = $assignedEventName
        type = "AUTO-045 score-manage"
        fee = "0"
        maxItemsPerAthlete = 2
        registrationStartTime = New-DateString -Value $now.AddMinutes(-10)
        registrationEndTime = New-DateString -Value $now.AddDays(2)
        eventStartTime = New-DateString -Value $now.AddDays(7)
        eventEndTime = New-DateString -Value $now.AddDays(8)
        projects = @(
            @{
                name = "AUTO-045 Score P1 " + (Get-Date -Format "HHmmss")
                category = "CUSTOM"
                limitation = "ALL"
                maxAttendance = 20
                startTime = New-DateString -Value $now.AddDays(7).AddHours(1)
                endTime = New-DateString -Value $now.AddDays(7).AddHours(2)
            },
            @{
                name = "AUTO-045 Score P2 " + (Get-Date -Format "HHmmss")
                category = "CUSTOM"
                limitation = "ALL"
                maxAttendance = 20
                startTime = New-DateString -Value $now.AddDays(7).AddHours(3)
                endTime = New-DateString -Value $now.AddDays(7).AddHours(4)
            }
        )
    }

    $controlEventBody = @{
        name = $controlEventName
        type = "AUTO-045 score-scope"
        fee = "0"
        maxItemsPerAthlete = 1
        registrationStartTime = New-DateString -Value $now.AddMinutes(-10)
        registrationEndTime = New-DateString -Value $now.AddDays(2)
        eventStartTime = New-DateString -Value $now.AddDays(9)
        eventEndTime = New-DateString -Value $now.AddDays(10)
        projects = @(
            @{
                name = "AUTO-045 Control P1 " + (Get-Date -Format "HHmmss")
                category = "CUSTOM"
                limitation = "ALL"
                maxAttendance = 20
                startTime = New-DateString -Value $now.AddDays(9).AddHours(1)
                endTime = New-DateString -Value $now.AddDays(9).AddHours(2)
            }
        )
    }

    Write-Step "Create assigned event and control event"
    $assignedCreateResp = Api-Post -Path "/api/event" -Headers $hSchool -Body $assignedEventBody
    Assert-Code -Name "create-assigned-event" -Resp $assignedCreateResp -Allowed @(200)
    $assignedEventId = [long]$assignedCreateResp.data
    $status.create_assigned_event = "pass"

    $controlCreateResp = Api-Post -Path "/api/event" -Headers $hSchool -Body $controlEventBody
    Assert-Code -Name "create-control-event" -Resp $controlCreateResp -Allowed @(200)
    $controlEventId = [long]$controlCreateResp.data
    $status.create_control_event = "pass"

    Write-Step "Bind EVENT_ADMIN only to assigned event"
    $bindResp = Api-Post -Path "/api/admin/events/$assignedEventId/admins" -Headers $hSchool -Body @([long]$eventAdmin.id)
    Assert-Code -Name "bind-event-admin" -Resp $bindResp -Allowed @(200)
    $status.bind_event_admin = "pass"

    Write-Step "Publish both events"
    $assignedPublishResp = Api-Put -Path "/api/event/$assignedEventId/status?status=OPEN" -Headers $hSchool -Body $null
    Assert-Code -Name "publish-assigned-event" -Resp $assignedPublishResp -Allowed @(200)
    $controlPublishResp = Api-Put -Path "/api/event/$controlEventId/status?status=OPEN" -Headers $hSchool -Body $null
    Assert-Code -Name "publish-control-event" -Resp $controlPublishResp -Allowed @(200)
    $status.publish_events = "pass"

    Write-Step "EVENT_ADMIN event/page should only show authorized events"
    $eventPageResp = Api-Get -Path "/api/event/page?currentPage=1&pageSize=200" -Headers $hEvent
    Assert-Code -Name "event-page-event-admin" -Resp $eventPageResp -Allowed @(200)
    $eventRecords = @()
    if ($eventPageResp.data -and $eventPageResp.data.records) { $eventRecords = @($eventPageResp.data.records) }
    $assignedVisible = @($eventRecords | Where-Object { [long]$_.id -eq $assignedEventId } | Select-Object -First 1)
    $controlVisible = @($eventRecords | Where-Object { [long]$_.id -eq $controlEventId } | Select-Object -First 1)
    if (-not $assignedVisible) {
        throw "event-page-event-admin failed: assigned event not visible"
    }
    if ($controlVisible) {
        throw "event-page-event-admin failed: unauthorized control event is visible"
    }
    $status.event_scope = "pass"

    Write-Step "Unauthorized score entry candidates should be rejected for unassigned event"
    $null = Expect-BusinessErrorCode -Name "score-entry-candidates-unassigned" -ExpectedCode 403 -Action {
        Invoke-RestMethod -Uri "$BackendUrl/api/score/entry-candidates/$controlEventId" -Method Get -Headers $hEvent -TimeoutSec 12
    }
    $status.unauthorized_candidates = "pass"

    Write-Step "USER applies athlete qualification for assigned event"
    $athleteApplyResp = Api-Post -Path "/api/athlete" -Headers $hUser -Body @{ eventId = [long]$assignedEventId }
    Assert-Code -Name "athlete-apply" -Resp $athleteApplyResp -Allowed @(200)
    $status.athlete_apply = "pass"

    Write-Step "EVENT_ADMIN approves athlete qualification"
    $application = Wait-ForAthleteApplication -Headers $hEvent -EventId $assignedEventId -UserId ([long]$user.id)
    $approveAthleteResp = Api-Post -Path "/api/event-admin/$assignedEventId/athlete-applications/$($application.id)/approve" -Headers $hEvent -Body $null
    Assert-Code -Name "athlete-approve" -Resp $approveAthleteResp -Allowed @(200)
    $status.athlete_approve = "pass"

    Write-Step "Resolve assigned event projects and apply both registrations"
    $projectResp = Api-Get -Path "/api/project/event/$assignedEventId" -Headers $hUser
    Assert-Code -Name "project-list-assigned-event" -Resp $projectResp -Allowed @(200)
    $projectRows = @()
    if ($projectResp.data) { $projectRows = @($projectResp.data) }
    $projectIds = @($projectRows | Select-Object -First 2 | ForEach-Object { [long]$_.id })
    if ($projectIds.Count -ne 2) {
        throw "assigned event does not expose two projects"
    }

    foreach ($projectId in $projectIds) {
        $applyResp = Api-Post -Path "/api/registration/apply/$projectId" -Headers $hUser -Body $null
        Assert-Code -Name "registration-apply-$projectId" -Resp $applyResp -Allowed @(200)
    }
    $status.registration_apply = "pass"

    Write-Step "EVENT_ADMIN approves both registrations"
    foreach ($projectId in $projectIds) {
        $pendingRegistration = Wait-ForRegistration -Headers $hEvent -AthleteId ([long]$user.id) -ProjectId ([long]$projectId)
        $registrationIds += [long]$pendingRegistration.id
        $approveRegistrationResp = Api-Put -Path "/api/registration/attend/$($pendingRegistration.id)?eventId=$assignedEventId" -Headers $hEvent -Body $null
        Assert-Code -Name "registration-approve-$projectId" -Resp $approveRegistrationResp -Allowed @(200)
    }
    $status.registration_approve = "pass"

    Write-Step "Score entry candidates before batch save should be two unentered approved rows"
    $candidateBeforeResp = Api-Get -Path "/api/score/entry-candidates/$assignedEventId" -Headers $hEvent
    Assert-Code -Name "score-entry-candidates-before" -Resp $candidateBeforeResp -Allowed @(200)
    $candidateBeforeRows = @()
    if ($candidateBeforeResp.data) { $candidateBeforeRows = @($candidateBeforeResp.data) }
    $targetCandidates = @($candidateBeforeRows | Where-Object { $registrationIds -contains [long]$_.registrationId })
    if ($targetCandidates.Count -ne 2) {
        throw "score-entry-candidates-before failed: expected 2 target rows, got $($targetCandidates.Count)"
    }
    if (@($targetCandidates | Where-Object { [string]$_.registrationStatus -notin @('APPROVED', 'CONFIRMED') }).Count -gt 0) {
        throw "score-entry-candidates-before failed: unexpected registration status"
    }
    if (@($targetCandidates | Where-Object { $null -ne $_.scoreId }).Count -gt 0) {
        throw "score-entry-candidates-before failed: expected no scoreId before save"
    }
    $status.score_candidates_before_save = "pass"

    Write-Step "Batch-save analogue: sequentially upsert two candidate rows"
    $rank = 1
    foreach ($registrationId in $registrationIds) {
        $scoreBody = @{
            registrationId = [long]$registrationId
            scoreValue = ("1{0}.0{1}" -f $rank, $rank)
            scoreRank = $rank
            remark = "batch-save-$rank"
        }
        $upsertResp = Api-Post -Path "/api/score/upsert" -Headers $hEvent -Body $scoreBody
        Assert-Code -Name "score-upsert-$registrationId" -Resp $upsertResp -Allowed @(200)
        $rank += 1
    }
    $status.batch_save = "pass"

    Write-Step "Candidates after batch save should all become draft rows"
    $candidateAfterSaveResp = Api-Get -Path "/api/score/entry-candidates/$assignedEventId" -Headers $hEvent
    Assert-Code -Name "score-entry-candidates-after-save" -Resp $candidateAfterSaveResp -Allowed @(200)
    $candidateAfterSaveRows = @()
    if ($candidateAfterSaveResp.data) { $candidateAfterSaveRows = @($candidateAfterSaveResp.data) }
    $savedTargets = @($candidateAfterSaveRows | Where-Object { $registrationIds -contains [long]$_.registrationId })
    if (@($savedTargets | Where-Object { $null -eq $_.scoreId -or [bool]$_.isPublished }).Count -gt 0) {
        throw "score-entry-candidates-after-save failed: expected saved draft rows with scoreId and not published"
    }
    $status.score_candidates_after_save = "pass"

    Write-Step "Publish scores and verify lock state"
    $publishResp = Api-Put -Path "/api/score/publish/$assignedEventId" -Headers $hEvent -Body $null
    Assert-Code -Name "score-publish" -Resp $publishResp -Allowed @(200)
    $status.score_publish = "pass"

    $candidateAfterPublishResp = Api-Get -Path "/api/score/entry-candidates/$assignedEventId" -Headers $hEvent
    Assert-Code -Name "score-entry-candidates-after-publish" -Resp $candidateAfterPublishResp -Allowed @(200)
    $candidateAfterPublishRows = @()
    if ($candidateAfterPublishResp.data) { $candidateAfterPublishRows = @($candidateAfterPublishResp.data) }
    $publishedTargets = @($candidateAfterPublishRows | Where-Object { $registrationIds -contains [long]$_.registrationId })
    if (@($publishedTargets | Where-Object { -not [bool]$_.isPublished }).Count -gt 0) {
        throw "score-entry-candidates-after-publish failed: expected all target rows published"
    }

    $null = Expect-BusinessErrorCode -Name "score-upsert-after-publish" -ExpectedCode 409 -Action {
        $lockedScoreBody = @{
            registrationId = [long]$registrationIds[0]
            scoreValue = "99.99"
            scoreRank = 9
            remark = "should-lock"
        } | ConvertTo-Json -Depth 5
        Invoke-RestMethod -Uri "$BackendUrl/api/score/upsert" -Method Post -Headers $hEvent -ContentType "application/json" -Body $lockedScoreBody -TimeoutSec 12
    }
    $status.score_lock = "pass"

    Write-Step "Published scores should be queryable in my/public views"
    $myScoresResp = Api-Get -Path "/api/score/my?eventId=$assignedEventId" -Headers $hUser
    Assert-Code -Name "score-my" -Resp $myScoresResp -Allowed @(200)
    $myScores = @()
    if ($myScoresResp.data) { $myScores = @($myScoresResp.data) }
    $myTargets = @($myScores | Where-Object { $registrationIds -contains [long]$_.registrationId })
    if ($myTargets.Count -ne 2) {
        throw "score-my failed: expected 2 published rows, got $($myTargets.Count)"
    }

    $publicScoresResp = Api-Get -Path "/api/score/public/$assignedEventId" -Headers $hUser
    Assert-Code -Name "score-public" -Resp $publicScoresResp -Allowed @(200)
    $publicRows = @()
    if ($publicScoresResp.data) {
        foreach ($group in $publicScoresResp.data.PSObject.Properties) {
            if ($group.Value) { $publicRows += @($group.Value) }
        }
    }
    $publicTargets = @($publicRows | Where-Object { $registrationIds -contains [long]$_.registrationId })
    if ($publicTargets.Count -ne 2) {
        throw "score-public failed: expected 2 published rows, got $($publicTargets.Count)"
    }
    $status.score_queries = "pass"

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
  - scripts/smoke-score-manage.ps1
  - git-ai/automation-route/runs/$RunId.md
key_changes:
  - Added a targeted score-manage smoke that verifies EVENT_ADMIN event visibility, score entry candidates, multi-row save, publish lock, and published query visibility on a newly created event.
  - The flow creates one assigned event and one unassigned control event so event/page and RequireEventAdmin boundaries can be asserted together.
test_results:
  - overall: $($status.overall)
  - steps:
      school_admin_login: $($status.school_admin_login)
      event_admin_login: $($status.event_admin_login)
      user_login: $($status.user_login)
      create_assigned_event: $($status.create_assigned_event)
      create_control_event: $($status.create_control_event)
      bind_event_admin: $($status.bind_event_admin)
      publish_events: $($status.publish_events)
      event_scope: $($status.event_scope)
      unauthorized_candidates: $($status.unauthorized_candidates)
      athlete_apply: $($status.athlete_apply)
      athlete_approve: $($status.athlete_approve)
      registration_apply: $($status.registration_apply)
      registration_approve: $($status.registration_approve)
      score_candidates_before_save: $($status.score_candidates_before_save)
      batch_save: $($status.batch_save)
      score_candidates_after_save: $($status.score_candidates_after_save)
      score_publish: $($status.score_publish)
      score_lock: $($status.score_lock)
      score_queries: $($status.score_queries)
artifacts:
  - assigned_event_id: "$assignedEventId"
  - assigned_event_name: "$assignedEventName"
  - control_event_id: "$controlEventId"
  - control_event_name: "$controlEventName"
  - registration_ids: "$($registrationIds -join ',')"
issue_inputs:
  - source: local-run
    signal: "$escapedError"
next_action: "Reuse this smoke after score-manage UI or score permission changes."
"@
    Set-Content -Path $runPath -Value $content -Encoding UTF8
    Write-Step "Run record written: $runPath"
}

if ($status.overall -eq "pass") {
    Write-Host "=== Score Manage Smoke Completed Successfully ==="
}
