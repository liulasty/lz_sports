param(
    [string]$BackendUrl = "http://localhost:8080",
    [string]$AccountsFile = "git-ai/automation-route/accounts/business-accounts-latest.json",
    [string]$SchoolAdminUsername = "init_school_admin_01",
    [string]$SchoolAdminPassword = "Admin12345",
    [string]$RunId = "",
    [string]$RelatedTaskId = "AUTO-046",
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$runFolder = Join-Path $root "git-ai/automation-route/runs"

function Write-Step {
    param([string]$Msg)
    Write-Host "[NEW-EVENT-BRANCHES] $Msg"
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

function Wait-ForAthleteApplicationByState {
    param(
        [hashtable]$Headers,
        [long]$EventId,
        [long]$UserId,
        [string]$State
    )

    for ($i = 0; $i -lt 10; $i++) {
        Start-Sleep -Milliseconds 500
        $path = "/api/event-admin/{0}/athlete-applications?status={1}&page=1&size=100" -f $EventId, $State
        $resp = Api-Get -Path $path -Headers $Headers
        Assert-Code -Name "athlete-applications-$State" -Resp $resp -Allowed @(200)
        $records = @()
        if ($resp.data -and $resp.data.records) { $records = @($resp.data.records) }
        $record = @($records | Where-Object { [long]$_.userId -eq [long]$UserId } | Select-Object -First 1)
        if ($record) {
            return $record
        }
    }

    throw "cannot find $State athlete application for userId=$UserId"
}

function Wait-ForRegistrationByState {
    param(
        [hashtable]$Headers,
        [long]$AthleteId,
        [long]$ProjectId,
        [string]$State
    )

    for ($i = 0; $i -lt 10; $i++) {
        Start-Sleep -Milliseconds 500
        $path = "/api/registration/page?currentPage=1&pageSize=100&status={0}" -f $State
        $resp = Api-Get -Path $path -Headers $Headers
        Assert-Code -Name "registration-page-$State" -Resp $resp -Allowed @(200)
        $records = @()
        if ($resp.data -and $resp.data.records) { $records = @($resp.data.records) }
        $record = @($records | Where-Object { [long]$_.athleteId -eq [long]$AthleteId -and [long]$_.itemId -eq [long]$ProjectId } | Select-Object -First 1)
        if ($record) {
            return $record
        }
    }

    throw "cannot find $State registration for athleteId=$AthleteId projectId=$ProjectId"
}

function Invoke-RegistrationApplyWithRetry {
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

function Flatten-PublicScores {
    param([object]$PublicData)
    $rows = @()
    if ($PublicData) {
        foreach ($group in $PublicData.PSObject.Properties) {
            if ($group.Value) {
                $rows += @($group.Value)
            }
        }
    }
    return $rows
}

if (-not $RunId) {
    $RunId = (Get-Date -Format "yyyy-MM-dd") + "-auto-046-new-event-branches"
}

$status = [ordered]@{
    school_admin_login = "pending"
    event_admin_login = "pending"
    user_login = "pending"
    create_event = "pending"
    bind_event_admin = "pending"
    publish_open = "pending"
    update_user_profile = "pending"
    athlete_apply_first = "pending"
    athlete_reject = "pending"
    athlete_rejected_status = "pending"
    athlete_reapply = "pending"
    athlete_approve = "pending"
    athlete_approved_status = "pending"
    registration_apply_first = "pending"
    registration_reject = "pending"
    registration_rejected_status = "pending"
    registration_reapply = "pending"
    registration_approve = "pending"
    registration_approved_status = "pending"
    score_upsert = "pending"
    score_publish = "pending"
    score_checks = "pending"
    overall = "pending"
    error = ""
}

$createdEventId = $null
$createdEventName = $null
$createdProjectId = $null
$createdProjectName = $null
$firstApplicationId = $null
$secondApplicationId = $null
$firstRegistrationId = $null
$secondRegistrationId = $null

Write-Host "=== LZ Sports New Event Branch Smoke ==="
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

    $now = Get-Date
    $eventStart = $now.AddDays(9)
    $eventEnd = $now.AddDays(10)
    $projectStart = $eventStart.AddHours(1)
    $projectEnd = $projectStart.AddHours(2)
    $createdEventName = "AUTO-046 Branch Event " + (Get-Date -Format "HHmmss")
    $createdProjectName = "AUTO-046 Branch Project " + (Get-Date -Format "HHmmss")

    $eventBody = @{
        name = $createdEventName
        type = "AUTO-046 smoke"
        fee = "0"
        maxItemsPerAthlete = 1
        registrationStartTime = New-DateString -Value $now.AddMinutes(-10)
        registrationEndTime = New-DateString -Value $now.AddDays(2)
        eventStartTime = New-DateString -Value $eventStart
        eventEndTime = New-DateString -Value $eventEnd
        projects = @(
            @{
                name = $createdProjectName
                category = "CUSTOM"
                limitation = "ALL"
                maxAttendance = 10
                startTime = New-DateString -Value $projectStart
                endTime = New-DateString -Value $projectEnd
            }
        )
    }

    Write-Step "Create new DRAFT event with one project"
    $createResp = Api-Post -Path "/api/event" -Headers $hSchool -Body $eventBody
    Assert-Code -Name "create-event" -Resp $createResp -Allowed @(200)
    $createdEventId = [long]$createResp.data
    $status.create_event = "pass"

    Write-Step "Bind existing EVENT_ADMIN to the new event"
    $bindResp = Api-Post -Path "/api/admin/events/$createdEventId/admins" -Headers $hSchool -Body @([long]$eventAdmin.id)
    Assert-Code -Name "bind-event-admin" -Resp $bindResp -Allowed @(200)
    $status.bind_event_admin = "pass"

    Write-Step "Publish new event to OPEN"
    $publishResp = Api-Put -Path "/api/event/$createdEventId/status?status=OPEN" -Headers $hSchool -Body $null
    Assert-Code -Name "publish-open" -Resp $publishResp -Allowed @(200)
    $status.publish_open = "pass"

    Write-Step "Resolve created project id from event project list"
    $projectResp = Api-Get -Path "/api/project/event/$createdEventId" -Headers $hUser
    Assert-Code -Name "project-list" -Resp $projectResp -Allowed @(200)
    $projects = @()
    if ($projectResp.data) { $projects = @($projectResp.data) }
    $targetProject = @($projects | Where-Object { [string]$_.itemName -eq $createdProjectName -or [string]$_.name -eq $createdProjectName } | Select-Object -First 1)
    if (-not $targetProject) {
        $targetProject = @($projects | Select-Object -First 1)
    }
    if (-not $targetProject) {
        throw "created event has no visible project"
    }
    $createdProjectId = [long]$targetProject.id

    Write-Step "Best-effort update USER profile when editable"
    $profileBody = @{
        userName = [string]$userSeed.username
        name = "自动用户" + (Get-Date -Format "HHmmss")
        gender = [string]([char]0x7537)
        contact = "1380000" + (Get-Date -Format "HHmmss")
        deptId = 11
    }
    $updateResp = Api-Post -Path "/api/auth/update" -Headers $hUser -Body $profileBody
    Assert-Code -Name "update-user-profile" -Resp $updateResp -Allowed @(200, 409)
    $status.update_user_profile = "pass"

    Write-Step "USER applies athlete qualification for the new event"
    $applyAthleteResp = Api-Post -Path "/api/athlete" -Headers $hUser -Body @{ eventId = [long]$createdEventId }
    Assert-Code -Name "athlete-apply-first" -Resp $applyAthleteResp -Allowed @(200)
    $status.athlete_apply_first = "pass"

    Write-Step "EVENT_ADMIN rejects the first athlete application"
    $firstApplication = Wait-ForAthleteApplicationByState -Headers $hEvent -EventId ([long]$createdEventId) -UserId ([long]$user.id) -State "PENDING"
    $firstApplicationId = [long]$firstApplication.id
    $rejectAthletePath = "/api/event-admin/{0}/athlete-applications/{1}/reject?reason={2}" -f $createdEventId, $firstApplicationId, "auto-smoke-reject"
    $rejectAthleteResp = Api-Post -Path $rejectAthletePath -Headers $hEvent -Body $null
    Assert-Code -Name "athlete-reject" -Resp $rejectAthleteResp -Allowed @(200)
    $status.athlete_reject = "pass"

    Write-Step "USER athlete status should become REJECTED"
    $athleteStatusPath = "/api/athlete/apply/{0}?eventId={1}" -f $user.id, $createdEventId
    $rejectedAthleteStatusResp = Api-Get -Path $athleteStatusPath -Headers $hUser
    Assert-Code -Name "athlete-status-rejected" -Resp $rejectedAthleteStatusResp -Allowed @(200)
    if (-not $rejectedAthleteStatusResp.data -or [string]$rejectedAthleteStatusResp.data.athleteState -ne "REJECTED") {
        throw "athlete-status-rejected failed: expected REJECTED"
    }
    $status.athlete_rejected_status = "pass"

    Write-Step "USER reapplies athlete qualification after rejection"
    Start-Sleep -Milliseconds 500
    $reapplyAthleteResp = Api-Post -Path "/api/athlete" -Headers $hUser -Body @{ eventId = [long]$createdEventId }
    Assert-Code -Name "athlete-reapply" -Resp $reapplyAthleteResp -Allowed @(200)
    $status.athlete_reapply = "pass"

    Write-Step "EVENT_ADMIN approves the second athlete application"
    $secondApplication = Wait-ForAthleteApplicationByState -Headers $hEvent -EventId ([long]$createdEventId) -UserId ([long]$user.id) -State "PENDING"
    $secondApplicationId = [long]$secondApplication.id
    $approveAthletePath = "/api/event-admin/{0}/athlete-applications/{1}/approve" -f $createdEventId, $secondApplicationId
    $approveAthleteResp = Api-Post -Path $approveAthletePath -Headers $hEvent -Body $null
    Assert-Code -Name "athlete-approve" -Resp $approveAthleteResp -Allowed @(200)
    $status.athlete_approve = "pass"

    Write-Step "USER athlete status should become APPROVED"
    $approvedAthleteStatusResp = Api-Get -Path $athleteStatusPath -Headers $hUser
    Assert-Code -Name "athlete-status-approved" -Resp $approvedAthleteStatusResp -Allowed @(200)
    if (-not $approvedAthleteStatusResp.data -or [string]$approvedAthleteStatusResp.data.athleteState -ne "APPROVED") {
        throw "athlete-status-approved failed: expected APPROVED"
    }
    $status.athlete_approved_status = "pass"

    Write-Step "Approved user applies for the project"
    $registrationApplyResp = Invoke-RegistrationApplyWithRetry -ProjectId ([long]$createdProjectId) -Headers $hUser -Name "registration-apply-first"
    Assert-Code -Name "registration-apply-first" -Resp $registrationApplyResp -Allowed @(200)
    $status.registration_apply_first = "pass"

    Write-Step "EVENT_ADMIN rejects the first registration"
    $firstRegistration = Wait-ForRegistrationByState -Headers $hEvent -AthleteId ([long]$user.id) -ProjectId ([long]$createdProjectId) -State "PENDING"
    $firstRegistrationId = [long]$firstRegistration.id
    $rejectRegistrationPath = "/api/registration/refuse/{0}?eventId={1}" -f $firstRegistrationId, $createdEventId
    $rejectRegistrationResp = Api-Put -Path $rejectRegistrationPath -Headers $hEvent -Body $null
    Assert-Code -Name "registration-reject" -Resp $rejectRegistrationResp -Allowed @(200)
    $status.registration_reject = "pass"

    Write-Step "USER registration page should show REJECTED status"
    $userRegistrationPage = Api-Get -Path "/api/registration/page?currentPage=1&pageSize=100" -Headers $hUser
    Assert-Code -Name "registration-page-after-reject" -Resp $userRegistrationPage -Allowed @(200)
    $userRegistrationRecords = @()
    if ($userRegistrationPage.data -and $userRegistrationPage.data.records) { $userRegistrationRecords = @($userRegistrationPage.data.records) }
    $rejectedRegistration = @($userRegistrationRecords | Where-Object { [long]$_.id -eq [long]$firstRegistrationId -and [string]$_.registrationStatus -eq "REJECTED" } | Select-Object -First 1)
    if (-not $rejectedRegistration) {
        throw "registration-rejected-status failed: expected REJECTED for registrationId=$firstRegistrationId"
    }
    $status.registration_rejected_status = "pass"

    Write-Step "USER reapplies for the same project after rejection"
    Start-Sleep -Seconds 1
    $registrationReapplyResp = Invoke-RegistrationApplyWithRetry -ProjectId ([long]$createdProjectId) -Headers $hUser -Name "registration-reapply"
    Assert-Code -Name "registration-reapply" -Resp $registrationReapplyResp -Allowed @(200)
    $status.registration_reapply = "pass"

    Write-Step "EVENT_ADMIN approves the second registration"
    $secondRegistration = Wait-ForRegistrationByState -Headers $hEvent -AthleteId ([long]$user.id) -ProjectId ([long]$createdProjectId) -State "PENDING"
    $secondRegistrationId = [long]$secondRegistration.id
    $approveRegistrationPath = "/api/registration/attend/{0}?eventId={1}" -f $secondRegistrationId, $createdEventId
    $approveRegistrationResp = Api-Put -Path $approveRegistrationPath -Headers $hEvent -Body $null
    Assert-Code -Name "registration-approve" -Resp $approveRegistrationResp -Allowed @(200)
    $status.registration_approve = "pass"

    Write-Step "USER registration page should show APPROVED status"
    $approvedRegistrationPage = Api-Get -Path "/api/registration/page?currentPage=1&pageSize=100" -Headers $hUser
    Assert-Code -Name "registration-page-after-approve" -Resp $approvedRegistrationPage -Allowed @(200)
    $approvedRegistrationRecords = @()
    if ($approvedRegistrationPage.data -and $approvedRegistrationPage.data.records) { $approvedRegistrationRecords = @($approvedRegistrationPage.data.records) }
    $approvedRegistration = @($approvedRegistrationRecords | Where-Object { [long]$_.id -eq [long]$secondRegistrationId -and [string]$_.registrationStatus -eq "APPROVED" } | Select-Object -First 1)
    if (-not $approvedRegistration) {
        throw "registration-approved-status failed: expected APPROVED for registrationId=$secondRegistrationId"
    }
    $status.registration_approved_status = "pass"

    Write-Step "EVENT_ADMIN upserts score for approved registration"
    $scoreBody = @{
        registrationId = [long]$secondRegistrationId
        scoreValue = "11.11"
        scoreRank = 1
        remark = "auto-branch-smoke"
    }
    $upsertScoreResp = Api-Post -Path "/api/score/upsert" -Headers $hEvent -Body $scoreBody
    Assert-Code -Name "score-upsert" -Resp $upsertScoreResp -Allowed @(200)
    $status.score_upsert = "pass"

    Write-Step "EVENT_ADMIN publishes scores for the new event"
    $publishScorePath = "/api/score/publish/{0}" -f $createdEventId
    $publishScoreResp = Api-Put -Path $publishScorePath -Headers $hEvent -Body $null
    Assert-Code -Name "score-publish" -Resp $publishScoreResp -Allowed @(200)
    $status.score_publish = "pass"

    Write-Step "USER should see published score in my/public queries"
    $myScoresResp = Api-Get -Path "/api/score/my?eventId=$createdEventId" -Headers $hUser
    Assert-Code -Name "score-my" -Resp $myScoresResp -Allowed @(200)
    $publicScoresResp = Api-Get -Path "/api/score/public/$createdEventId" -Headers $hUser
    Assert-Code -Name "score-public" -Resp $publicScoresResp -Allowed @(200)
    $myScores = @()
    if ($myScoresResp.data) { $myScores = @($myScoresResp.data) }
    $myScore = @($myScores | Where-Object { [long]$_.registrationId -eq [long]$secondRegistrationId } | Select-Object -First 1)
    if (-not $myScore) {
        throw "score-my missing published score for registrationId=$secondRegistrationId"
    }
    $publicRows = Flatten-PublicScores -PublicData $publicScoresResp.data
    $publicScore = @($publicRows | Where-Object { [long]$_.registrationId -eq [long]$secondRegistrationId } | Select-Object -First 1)
    if (-not $publicScore) {
        throw "score-public missing published score for registrationId=$secondRegistrationId"
    }
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
    $eventIdText = if ($createdEventId) { [string]$createdEventId } else { "none" }
    $projectIdText = if ($createdProjectId) { [string]$createdProjectId } else { "none" }
    $content = @"
run_id: $RunId
related_task_id: $RelatedTaskId
changed_files:
  - scripts/smoke-new-event-branches.ps1
  - git-ai/automation-route/runs/$RunId.md
key_changes:
  - Added a targeted smoke that covers reject-athlete, reapply-athlete, reject-registration, reapply-registration, and score publish branches on a newly created event.
  - The flow reuses existing SCHOOL_ADMIN, EVENT_ADMIN, and USER accounts and keeps all assertions inside the same event lifecycle.
test_results:
  - overall: $($status.overall)
  - steps:
      school_admin_login: $($status.school_admin_login)
      event_admin_login: $($status.event_admin_login)
      user_login: $($status.user_login)
      create_event: $($status.create_event)
      bind_event_admin: $($status.bind_event_admin)
      publish_open: $($status.publish_open)
      update_user_profile: $($status.update_user_profile)
      athlete_apply_first: $($status.athlete_apply_first)
      athlete_reject: $($status.athlete_reject)
      athlete_rejected_status: $($status.athlete_rejected_status)
      athlete_reapply: $($status.athlete_reapply)
      athlete_approve: $($status.athlete_approve)
      athlete_approved_status: $($status.athlete_approved_status)
      registration_apply_first: $($status.registration_apply_first)
      registration_reject: $($status.registration_reject)
      registration_rejected_status: $($status.registration_rejected_status)
      registration_reapply: $($status.registration_reapply)
      registration_approve: $($status.registration_approve)
      registration_approved_status: $($status.registration_approved_status)
      score_upsert: $($status.score_upsert)
      score_publish: $($status.score_publish)
      score_checks: $($status.score_checks)
artifacts:
  - event_id: "$eventIdText"
  - event_name: "$createdEventName"
  - project_id: "$projectIdText"
  - project_name: "$createdProjectName"
  - first_application_id: "$firstApplicationId"
  - second_application_id: "$secondApplicationId"
  - first_registration_id: "$firstRegistrationId"
  - second_registration_id: "$secondRegistrationId"
issue_inputs:
  - source: local-run
    signal: "$escapedError"
next_action: "Reuse this smoke after athlete audit, registration audit, or score workflow changes."
"@
    Set-Content -Path $runPath -Value $content -Encoding UTF8
    Write-Step "Run record written: $runPath"
}

if ($status.overall -eq "pass") {
    Write-Host "=== New Event Branch Smoke Completed Successfully ==="
}
