param(
    [string]$BackendUrl = "http://localhost:8080",
    [string]$AccountsFile = "git-ai/automation-route/accounts/business-accounts-latest.json",
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
    Write-Host "[NEW-EVENT-BIZ] $Msg"
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

function Resolve-AbsolutePath {
    param([string]$Path)
    if ([System.IO.Path]::IsPathRooted($Path)) {
        return $Path
    }
    return Join-Path $root $Path
}

function Wait-ForApplication {
    param(
        [hashtable]$Headers,
        [long]$EventId,
        [long]$UserId
    )

    for ($i = 0; $i -lt 10; $i++) {
        Start-Sleep -Milliseconds 500
        $resp = Api-Get -Path "/api/event-admin/$EventId/athlete-applications?status=PENDING&page=1&size=100" -Headers $Headers
        Assert-Code -Name "athlete-applications-page" -Resp $resp -Allowed @(200)
        $records = @()
        if ($resp.data -and $resp.data.records) { $records = @($resp.data.records) }
        $record = @($records | Where-Object { [long]$_.userId -eq [long]$UserId } | Select-Object -First 1)
        if ($record) {
            return $record
        }
    }

    throw "cannot find pending athlete application for userId=$UserId"
}

function Wait-ForRegistration {
    param(
        [hashtable]$Headers,
        [long]$AthleteId,
        [long]$ProjectId
    )

    for ($i = 0; $i -lt 10; $i++) {
        Start-Sleep -Milliseconds 500
        $resp = Api-Get -Path "/api/registration/page?currentPage=1&pageSize=100&status=PENDING" -Headers $Headers
        Assert-Code -Name "registration-page-pending" -Resp $resp -Allowed @(200)
        $records = @()
        if ($resp.data -and $resp.data.records) { $records = @($resp.data.records) }
        $record = @($records | Where-Object { [long]$_.athleteId -eq [long]$AthleteId -and [long]$_.itemId -eq [long]$ProjectId } | Select-Object -First 1)
        if ($record) {
            return $record
        }
    }

    throw "cannot find pending registration for athleteId=$AthleteId projectId=$ProjectId"
}

if (-not $RunId) {
    $RunId = (Get-Date -Format "yyyy-MM-dd") + "-auto-045-new-event-business"
}

$status = [ordered]@{
    school_admin_login = "pending"
    event_admin_login = "pending"
    user_login = "pending"
    create_event = "pending"
    bind_event_admin = "pending"
    publish_open = "pending"
    update_user_profile = "pending"
    athlete_apply = "pending"
    athlete_approve = "pending"
    athlete_status = "pending"
    registration_apply = "pending"
    registration_approve = "pending"
    registration_status = "pending"
    overall = "pending"
    error = ""
}

$createdEventId = $null
$createdEventName = $null
$createdProjectId = $null
$createdProjectName = $null
$userApplicationId = $null
$registrationId = $null

Write-Host "=== LZ Sports New Event Business Smoke ==="
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
    $eventStart = $now.AddDays(7)
    $eventEnd = $now.AddDays(8)
    $projectStart = $eventStart.AddHours(1)
    $projectEnd = $projectStart.AddHours(2)
    $createdEventName = "AUTO-045 New Event " + (Get-Date -Format "HHmmss")
    $createdProjectName = "AUTO-045 Project " + (Get-Date -Format "HHmmss")

    $eventBody = @{
        name = $createdEventName
        type = "AUTO-045 smoke"
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

    Write-Step "Ensure USER profile is complete for athlete application"
    $infoResp = Api-Get -Path "/api/auth/info" -Headers $hUser
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
    if ($needsProfileUpdate) {
        $profileBody = @{
            userName = [string]$userSeed.username
            name = "自动用户" + (Get-Date -Format "HHmmss")
            gender = [string]([char]0x7537)
            contact = "1380000" + (Get-Date -Format "HHmmss")
            deptId = 11
        }
        $updateResp = Api-Post -Path "/api/auth/update" -Headers $hUser -Body $profileBody
        Assert-Code -Name "update-user-profile" -Resp $updateResp -Allowed @(200, 409)
    }
    $status.update_user_profile = "pass"

    Write-Step "USER applies to become athlete for the new event"
    $applyAthleteResp = Api-Post -Path "/api/athlete" -Headers $hUser -Body @{ eventId = [long]$createdEventId }
    Assert-Code -Name "athlete-apply" -Resp $applyAthleteResp -Allowed @(200)
    $status.athlete_apply = "pass"

    Write-Step "EVENT_ADMIN approves the athlete application"
    $application = Wait-ForApplication -Headers $hEvent -EventId ([long]$createdEventId) -UserId ([long]$user.id)
    $userApplicationId = [long]$application.id
    $approveAthleteResp = Api-Post -Path "/api/event-admin/$createdEventId/athlete-applications/$userApplicationId/approve" -Headers $hEvent -Body $null
    Assert-Code -Name "athlete-approve" -Resp $approveAthleteResp -Allowed @(200)
    $status.athlete_approve = "pass"

    Write-Step "USER should now have APPROVED athlete qualification for the new event"
    $athleteStatusPath = "/api/athlete/apply/{0}?eventId={1}" -f $user.id, $createdEventId
    $athleteStatusResp = Api-Get -Path $athleteStatusPath -Headers $hUser
    Assert-Code -Name "athlete-status" -Resp $athleteStatusResp -Allowed @(200)
    if (-not $athleteStatusResp.data -or [string]$athleteStatusResp.data.athleteState -ne "APPROVED") {
        throw "athlete-status failed: expected APPROVED"
    }
    $status.athlete_status = "pass"

    Write-Step "Approved USER applies for the event project"
    $registrationApplyResp = Api-Post -Path "/api/registration/apply/$createdProjectId" -Headers $hUser -Body $null
    Assert-Code -Name "registration-apply" -Resp $registrationApplyResp -Allowed @(200)
    $status.registration_apply = "pass"

    Write-Step "EVENT_ADMIN approves the pending registration"
    $registration = Wait-ForRegistration -Headers $hEvent -AthleteId ([long]$user.id) -ProjectId ([long]$createdProjectId)
    $registrationId = [long]$registration.id
    $approveRegistrationPath = "/api/registration/attend/{0}?eventId={1}" -f $registrationId, $createdEventId
    $approveRegistrationResp = Api-Put -Path $approveRegistrationPath -Headers $hEvent -Body $null
    Assert-Code -Name "registration-approve" -Resp $approveRegistrationResp -Allowed @(200)
    $status.registration_approve = "pass"

    Write-Step "USER registration page should show APPROVED status"
    $registrationPageResp = Api-Get -Path "/api/registration/page?currentPage=1&pageSize=100" -Headers $hUser
    Assert-Code -Name "registration-page-user" -Resp $registrationPageResp -Allowed @(200)
    $registrationRecords = @()
    if ($registrationPageResp.data -and $registrationPageResp.data.records) { $registrationRecords = @($registrationPageResp.data.records) }
    $approvedRecord = @($registrationRecords | Where-Object { [long]$_.id -eq [long]$registrationId -and [string]$_.registrationStatus -eq "APPROVED" } | Select-Object -First 1)
    if (-not $approvedRecord) {
        throw "registration-status failed: missing APPROVED record for registrationId=$registrationId"
    }
    $status.registration_status = "pass"

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
    $applicationIdText = if ($userApplicationId) { [string]$userApplicationId } else { "none" }
    $registrationIdText = if ($registrationId) { [string]$registrationId } else { "none" }
    $content = @"
run_id: $RunId
related_task_id: $RelatedTaskId
changed_files:
  - scripts/smoke-new-event-business.ps1
  - git-ai/automation-route/runs/$RunId.md
key_changes:
  - Added a targeted smoke that reuses existing SCHOOL_ADMIN, EVENT_ADMIN and USER accounts to complete the new event business chain.
  - The flow covers create event, bind event admin, publish OPEN, user athlete application, event-admin approval, project registration, and registration approval.
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
      athlete_apply: $($status.athlete_apply)
      athlete_approve: $($status.athlete_approve)
      athlete_status: $($status.athlete_status)
      registration_apply: $($status.registration_apply)
      registration_approve: $($status.registration_approve)
      registration_status: $($status.registration_status)
artifacts:
  - event_id: "$eventIdText"
  - event_name: "$createdEventName"
  - project_id: "$projectIdText"
  - project_name: "$createdProjectName"
  - athlete_application_id: "$applicationIdText"
  - registration_id: "$registrationIdText"
issue_inputs:
  - source: local-run
    signal: "$escapedError"
next_action: "Reuse this smoke to validate new-event lifecycle, athlete qualification, and registration approval together."
"@
    Set-Content -Path $runPath -Value $content -Encoding UTF8
    Write-Step "Run record written: $runPath"
}

if ($status.overall -eq "pass") {
    Write-Host "=== New Event Business Smoke Completed Successfully ==="
}
