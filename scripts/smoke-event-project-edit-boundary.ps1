param(
    [string]$BackendUrl = "http://localhost:8080",
    [string]$AccountsFile = "git-ai/automation-route/accounts/business-accounts-latest.json",
    [string]$SchoolAdminUsername = "init_school_admin_01",
    [string]$SchoolAdminPassword = "Admin12345",
    [string]$RunId = "",
    [string]$RelatedTaskId = "AUTO-055",
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$runFolder = Join-Path $root "git-ai/automation-route/runs"

function Write-Step {
    param([string]$Msg)
    Write-Host "[EVENT-PROJECT-EDIT] $Msg"
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
        token = $token
        role = [string]$resp.data.role
    }
}

function Resolve-SchoolAdminLogin {
    $candidates = @(
        @{ username = $SchoolAdminUsername; password = $SchoolAdminPassword },
        @{ username = $SchoolAdminUsername; password = "admin123" },
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

function Api-Delete {
    param([string]$Path, [hashtable]$Headers)
    return Invoke-RestMethod -Uri "$BackendUrl$Path" -Method Delete -Headers $Headers -TimeoutSec 12
}

function Invoke-Capture {
    param([scriptblock]$Action)
    try {
        return & $Action
    } catch {
        $raw = $_.ErrorDetails.Message
        if (-not [string]::IsNullOrWhiteSpace($raw)) {
            return ($raw | ConvertFrom-Json)
        }
        throw
    }
}

function Resolve-AbsolutePath {
    param([string]$Path)
    if ([System.IO.Path]::IsPathRooted($Path)) { return $Path }
    return Join-Path $root $Path
}

function New-DateString {
    param([datetime]$Value)
    return $Value.ToString("yyyy-MM-dd HH:mm:ss")
}

if (-not $RunId) {
    $RunId = (Get-Date -Format "yyyy-MM-dd") + "-auto-055-event-project-edit-boundary"
}

$status = [ordered]@{
    school_admin_login = "pending"
    event_admin_login = "pending"
    user_login = "pending"
    create_event = "pending"
    bind_event_admin = "pending"
    publish_open = "pending"
    school_admin_event_update = "pending"
    user_event_update_forbidden = "pending"
    event_admin_event_update = "pending"
    school_admin_project_update = "pending"
    school_admin_project_invalid_time = "pending"
    user_project_update_forbidden = "pending"
    cleanup_event_delete = "pending"
    overall = "pending"
    error = ""
}

$createdEventId = $null
$createdProjectId = $null
$createdEventName = $null
$cleanupReady = $false

Write-Host "=== LZ Sports Event/Project Edit Boundary Smoke ==="
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
    $eventStart = $now.AddDays(12)
    $eventEnd = $now.AddDays(13)
    $projectStart = $eventStart.AddHours(1)
    $projectEnd = $projectStart.AddHours(2)
    $createdEventName = "AUTO-055 Edit Boundary " + (Get-Date -Format "HHmmss")

    $eventBody = @{
        name = $createdEventName
        type = "AUTO-055 smoke"
        fee = "0"
        maxItemsPerAthlete = 2
        registrationStartTime = New-DateString -Value $now.AddMinutes(-5)
        registrationEndTime = New-DateString -Value $now.AddDays(2)
        eventStartTime = New-DateString -Value $eventStart
        eventEndTime = New-DateString -Value $eventEnd
        projects = @(
            @{
                name = "AUTO-055 Project " + (Get-Date -Format "HHmmss")
                category = "CUSTOM"
                limitation = "ALL"
                maxAttendance = 10
                startTime = New-DateString -Value $projectStart
                endTime = New-DateString -Value $projectEnd
            }
        )
    }

    Write-Step "Create new event for edit boundary checks"
    $createResp = Api-Post -Path "/api/event" -Headers $hSchool -Body $eventBody
    Assert-Code -Name "create-event" -Resp $createResp -Allowed @(200)
    $createdEventId = [long]$createResp.data
    $status.create_event = "pass"

    Write-Step "Bind EVENT_ADMIN to created event"
    $bindResp = Api-Post -Path "/api/admin/events/$createdEventId/admins" -Headers $hSchool -Body @([long]$eventAdmin.id)
    Assert-Code -Name "bind-event-admin" -Resp $bindResp -Allowed @(200)
    $status.bind_event_admin = "pass"

    Write-Step "Publish created event to OPEN"
    $publishResp = Api-Put -Path "/api/event/$createdEventId/status?status=OPEN" -Headers $hSchool -Body $null
    Assert-Code -Name "publish-open" -Resp $publishResp -Allowed @(200)
    $status.publish_open = "pass"

    $projectResp = Api-Get -Path "/api/project/event/$createdEventId" -Headers $hUser
    Assert-Code -Name "project-list" -Resp $projectResp -Allowed @(200)
    $projects = @()
    if ($projectResp.data) { $projects = @($projectResp.data) }
    $targetProject = @($projects | Select-Object -First 1)
    if (-not $targetProject) { throw "project-list failed: no project found" }
    $createdProjectId = [long]$targetProject.id

    Write-Step "SCHOOL_ADMIN updates event fields successfully"
    $schoolUpdateResp = Api-Put -Path "/api/event/$createdEventId" -Headers $hSchool -Body @{
        name = "$createdEventName updated"
        maxItemsPerAthlete = 3
    }
    Assert-Code -Name "school-admin-event-update" -Resp $schoolUpdateResp -Allowed @(200)
    $status.school_admin_event_update = "pass"

    Write-Step "USER cannot update event"
    $userUpdateResp = Invoke-Capture -Action { Api-Put -Path "/api/event/$createdEventId" -Headers $hUser -Body @{ name = "USER should fail" } }
    Assert-Code -Name "user-event-update-forbidden" -Resp $userUpdateResp -Allowed @(403)
    $status.user_event_update_forbidden = "pass"

    Write-Step "EVENT_ADMIN can update assigned event"
    $eventAdminUpdateResp = Api-Put -Path "/api/event/$createdEventId" -Headers $hEvent -Body @{ type = "AUTO-055 updated by event admin" }
    Assert-Code -Name "event-admin-event-update" -Resp $eventAdminUpdateResp -Allowed @(200)
    $status.event_admin_event_update = "pass"

    Write-Step "SCHOOL_ADMIN updates project with valid fields"
    $projectUpdateResp = Api-Put -Path "/api/project/$createdProjectId" -Headers $hSchool -Body @{
        name = "AUTO-055 Project Updated"
        limitation = "MALE"
        maxAttendance = 12
    }
    Assert-Code -Name "school-admin-project-update" -Resp $projectUpdateResp -Allowed @(200)
    $status.school_admin_project_update = "pass"

    Write-Step "Project update rejects invalid time window"
    $badProjectResp = Invoke-Capture -Action {
        Api-Put -Path "/api/project/$createdProjectId" -Headers $hSchool -Body @{
            startTime = New-DateString -Value $eventStart.AddHours(5)
            endTime = New-DateString -Value $eventStart.AddHours(4)
        }
    }
    Assert-Code -Name "school-admin-project-invalid-time" -Resp $badProjectResp -Allowed @(400, 409)
    $status.school_admin_project_invalid_time = "pass"

    Write-Step "USER cannot update project"
    $userProjectUpdateResp = Invoke-Capture -Action { Api-Put -Path "/api/project/$createdProjectId" -Headers $hUser -Body @{ name = "USER project update" } }
    Assert-Code -Name "user-project-update-forbidden" -Resp $userProjectUpdateResp -Allowed @(403)
    $status.user_project_update_forbidden = "pass"

    Write-Step "Cleanup by moving event back to DRAFT then delete"
    $futureResp = Api-Put -Path "/api/event/$createdEventId" -Headers $hSchool -Body @{
        registrationStartTime = New-DateString -Value $now.AddDays(7)
        registrationEndTime = New-DateString -Value $now.AddDays(8)
        eventStartTime = New-DateString -Value $now.AddDays(9)
        eventEndTime = New-DateString -Value $now.AddDays(10)
    }
    Assert-Code -Name "cleanup-update-times" -Resp $futureResp -Allowed @(200)
    $withdrawResp = Api-Post -Path "/api/admin/events/$createdEventId/withdraw" -Headers $hSchool -Body $null
    Assert-Code -Name "cleanup-withdraw" -Resp $withdrawResp -Allowed @(200)
    $cleanupReady = $true
    $deleteResp = Api-Delete -Path "/api/event/$createdEventId" -Headers $hSchool
    Assert-Code -Name "cleanup-delete-event" -Resp $deleteResp -Allowed @(200)
    $cleanupReady = $false
    $status.cleanup_event_delete = "pass"

    $status.overall = "pass"
} catch {
    $status.overall = "fail"
    $status.error = $_.Exception.Message
    throw
} finally {
    if ($cleanupReady -and $createdEventId) {
        try {
            $deleteResp = Api-Delete -Path "/api/event/$createdEventId" -Headers $hSchool
            Assert-Code -Name "cleanup-delete-event-finally" -Resp $deleteResp -Allowed @(200)
            $status.cleanup_event_delete = "pass"
        } catch {
            Write-Step "Cleanup delete failed: $($_.Exception.Message)"
        }
    }

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
  - scripts/smoke-event-project-edit-boundary.ps1
  - git-ai/automation-route/runs/$RunId.md
key_changes:
  - Added event/project edit boundary smoke for SCHOOL_ADMIN, EVENT_ADMIN and USER roles.
  - Verified valid event/project updates, invalid project time rejection, and USER write-path forbidden checks.
test_results:
  - overall: $($status.overall)
  - steps:
      school_admin_login: $($status.school_admin_login)
      event_admin_login: $($status.event_admin_login)
      user_login: $($status.user_login)
      create_event: $($status.create_event)
      bind_event_admin: $($status.bind_event_admin)
      publish_open: $($status.publish_open)
      school_admin_event_update: $($status.school_admin_event_update)
      user_event_update_forbidden: $($status.user_event_update_forbidden)
      event_admin_event_update: $($status.event_admin_event_update)
      school_admin_project_update: $($status.school_admin_project_update)
      school_admin_project_invalid_time: $($status.school_admin_project_invalid_time)
      user_project_update_forbidden: $($status.user_project_update_forbidden)
      cleanup_event_delete: $($status.cleanup_event_delete)
artifacts:
  - event_id: "$eventIdText"
  - project_id: "$projectIdText"
issue_inputs:
  - source: local-run
    signal: "$escapedError"
next_action: "Reuse this smoke when event/project update permissions or validation rules change."
"@
    Set-Content -Path $runPath -Value $content -Encoding UTF8
    Write-Step "Run record written: $runPath"
}

if ($status.overall -eq "pass") {
    Write-Host "=== Event/Project Edit Boundary Smoke Completed Successfully ==="
}
