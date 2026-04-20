param(
    [string]$BackendUrl = "http://localhost:8080",
    [string]$AccountsFile = "git-ai/automation-route/accounts/business-accounts-latest.json",
    [ValidateSet("cross-event-edit", "unbind-permission-shift", "project-write-boundary")]
    [string]$Scenario = "cross-event-edit",
    [string]$SchoolAdminUsername = "init_school_admin_01",
    [string]$SchoolAdminPassword = "Admin12345",
    [string]$RunId = "",
    [string]$RelatedTaskId = "AUTO-056",
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$runFolder = Join-Path $root "git-ai/automation-route/runs"

function Write-Step {
    param([string]$Msg)
    Write-Host "[EVENT-ADMIN-PERM] $Msg"
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

function New-EventWithProject {
    param(
        [hashtable]$SchoolHeaders,
        [hashtable]$ViewerHeaders,
        [string]$Prefix
    )
    $now = Get-Date
    $eventStart = $now.AddDays(11)
    $eventEnd = $now.AddDays(12)
    $projectStart = $eventStart.AddHours(1)
    $projectEnd = $projectStart.AddHours(2)
    $eventName = "$Prefix Event " + (Get-Date -Format "HHmmssfff")
    $projectName = "$Prefix Project " + (Get-Date -Format "HHmmssfff")
    $eventBody = @{
        name = $eventName
        type = "$Prefix smoke"
        fee = "0"
        maxItemsPerAthlete = 2
        registrationStartTime = New-DateString -Value $now.AddMinutes(-5)
        registrationEndTime = New-DateString -Value $now.AddDays(2)
        eventStartTime = New-DateString -Value $eventStart
        eventEndTime = New-DateString -Value $eventEnd
        projects = @(
            @{
                name = $projectName
                category = "CUSTOM"
                limitation = "ALL"
                maxAttendance = 20
                startTime = New-DateString -Value $projectStart
                endTime = New-DateString -Value $projectEnd
            }
        )
    }
    $createResp = Api-Post -Path "/api/event" -Headers $SchoolHeaders -Body $eventBody
    Assert-Code -Name "create-$Prefix-event" -Resp $createResp -Allowed @(200)
    $eventId = [long]$createResp.data
    $publishResp = Api-Put -Path "/api/event/$eventId/status?status=OPEN" -Headers $SchoolHeaders -Body $null
    Assert-Code -Name "publish-$Prefix-event" -Resp $publishResp -Allowed @(200)
    $projectResp = Api-Get -Path "/api/project/event/$eventId" -Headers $ViewerHeaders
    Assert-Code -Name "project-list-$Prefix-event" -Resp $projectResp -Allowed @(200)
    $records = @()
    if ($projectResp.data) { $records = @($projectResp.data) }
    $targetProject = @($records | Select-Object -First 1)
    if (-not $targetProject) { throw "project-list-$Prefix-event failed: empty" }
    return [PSCustomObject]@{
        eventId = $eventId
        projectId = [long]$targetProject.id
        eventName = $eventName
        projectName = $projectName
    }
}

function Cleanup-Event {
    param(
        [long]$EventId,
        [hashtable]$SchoolHeaders
    )
    if (-not $EventId) { return }
    $now = Get-Date
    $updateResp = Api-Put -Path "/api/event/$EventId" -Headers $SchoolHeaders -Body @{
        registrationStartTime = New-DateString -Value $now.AddDays(7)
        registrationEndTime = New-DateString -Value $now.AddDays(8)
        eventStartTime = New-DateString -Value $now.AddDays(9)
        eventEndTime = New-DateString -Value $now.AddDays(10)
    }
    Assert-Code -Name "cleanup-update-$EventId" -Resp $updateResp -Allowed @(200)
    $withdrawResp = Api-Post -Path "/api/admin/events/$EventId/withdraw" -Headers $SchoolHeaders -Body $null
    Assert-Code -Name "cleanup-withdraw-$EventId" -Resp $withdrawResp -Allowed @(200)
    $deleteResp = Api-Delete -Path "/api/event/$EventId" -Headers $SchoolHeaders
    Assert-Code -Name "cleanup-delete-$EventId" -Resp $deleteResp -Allowed @(200)
}

if (-not $RunId) {
    $RunId = (Get-Date -Format "yyyy-MM-dd") + "-auto-056-event-admin-permission-shift"
}

$status = [ordered]@{
    school_admin_login = "pending"
    event_admin_login = "pending"
    user_login = "pending"
    scenario_main = "pending"
    cleanup = "pending"
    overall = "pending"
    error = ""
}
$artifactEventIds = @()
$artifactProjectIds = @()

Write-Host "=== LZ Sports Event Admin Permission Shift Smoke ==="
Write-Host "RunId       : $RunId"
Write-Host "Scenario    : $Scenario"
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

    if ($Scenario -eq "cross-event-edit") {
        Write-Step "Scenario: EVENT_ADMIN cannot edit unbound event"
        $bound = New-EventWithProject -SchoolHeaders $hSchool -ViewerHeaders $hUser -Prefix "AUTO-056-bound"
        $unbound = New-EventWithProject -SchoolHeaders $hSchool -ViewerHeaders $hUser -Prefix "AUTO-056-unbound"
        $artifactEventIds += $bound.eventId
        $artifactEventIds += $unbound.eventId
        $artifactProjectIds += $bound.projectId
        $artifactProjectIds += $unbound.projectId

        $bindResp = Api-Post -Path "/api/admin/events/$($bound.eventId)/admins" -Headers $hSchool -Body @([long]$eventAdmin.id)
        Assert-Code -Name "bind-event-admin-bound" -Resp $bindResp -Allowed @(200)
        $okResp = Api-Put -Path "/api/event/$($bound.eventId)" -Headers $hEvent -Body @{ type = "AUTO-056 bound update ok" }
        Assert-Code -Name "event-admin-update-bound-event" -Resp $okResp -Allowed @(200)
        $forbiddenResp = Invoke-Capture -Action { Api-Put -Path "/api/event/$($unbound.eventId)" -Headers $hEvent -Body @{ type = "AUTO-056 should fail" } }
        Assert-Code -Name "event-admin-update-unbound-event-forbidden" -Resp $forbiddenResp -Allowed @(403)
        $status.scenario_main = "pass"
    } elseif ($Scenario -eq "unbind-permission-shift") {
        Write-Step "Scenario: unbind should revoke EVENT_ADMIN edit permission"
        $eventInfo = New-EventWithProject -SchoolHeaders $hSchool -ViewerHeaders $hUser -Prefix "AUTO-057-shift"
        $artifactEventIds += $eventInfo.eventId
        $artifactProjectIds += $eventInfo.projectId
        $bindResp = Api-Post -Path "/api/admin/events/$($eventInfo.eventId)/admins" -Headers $hSchool -Body @([long]$eventAdmin.id)
        Assert-Code -Name "bind-event-admin-shift" -Resp $bindResp -Allowed @(200)
        $okResp = Api-Put -Path "/api/event/$($eventInfo.eventId)" -Headers $hEvent -Body @{ type = "AUTO-057 before unbind" }
        Assert-Code -Name "event-admin-update-before-unbind" -Resp $okResp -Allowed @(200)
        $unbindResp = Api-Delete -Path "/api/admin/events/$($eventInfo.eventId)/admins/$($eventAdmin.id)" -Headers $hSchool
        Assert-Code -Name "unbind-event-admin" -Resp $unbindResp -Allowed @(200)
        $forbiddenResp = Invoke-Capture -Action { Api-Put -Path "/api/event/$($eventInfo.eventId)" -Headers $hEvent -Body @{ type = "AUTO-057 after unbind" } }
        Assert-Code -Name "event-admin-update-after-unbind-forbidden" -Resp $forbiddenResp -Allowed @(403)
        $status.scenario_main = "pass"
    } else {
        Write-Step "Scenario: EVENT_ADMIN/USER cannot write project endpoints"
        $eventInfo = New-EventWithProject -SchoolHeaders $hSchool -ViewerHeaders $hUser -Prefix "AUTO-058-project"
        $artifactEventIds += $eventInfo.eventId
        $artifactProjectIds += $eventInfo.projectId
        $bindResp = Api-Post -Path "/api/admin/events/$($eventInfo.eventId)/admins" -Headers $hSchool -Body @([long]$eventAdmin.id)
        Assert-Code -Name "bind-event-admin-project" -Resp $bindResp -Allowed @(200)
        $schoolUpdateResp = Api-Put -Path "/api/project/$($eventInfo.projectId)" -Headers $hSchool -Body @{ name = "AUTO-058 updated by school admin" }
        Assert-Code -Name "school-admin-project-update" -Resp $schoolUpdateResp -Allowed @(200)
        $eventForbiddenResp = Invoke-Capture -Action { Api-Put -Path "/api/project/$($eventInfo.projectId)" -Headers $hEvent -Body @{ name = "AUTO-058 event admin denied" } }
        Assert-Code -Name "event-admin-project-update-forbidden" -Resp $eventForbiddenResp -Allowed @(403)
        $userForbiddenResp = Invoke-Capture -Action { Api-Put -Path "/api/project/$($eventInfo.projectId)" -Headers $hUser -Body @{ name = "AUTO-058 user denied" } }
        Assert-Code -Name "user-project-update-forbidden" -Resp $userForbiddenResp -Allowed @(403)
        $status.scenario_main = "pass"
    }

    foreach ($eid in $artifactEventIds) {
        Cleanup-Event -EventId ([long]$eid) -SchoolHeaders $hSchool
    }
    $status.cleanup = "pass"
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
    $eventIdsText = if ($artifactEventIds.Count -gt 0) { ($artifactEventIds -join ",") } else { "none" }
    $projectIdsText = if ($artifactProjectIds.Count -gt 0) { ($artifactProjectIds -join ",") } else { "none" }
    $content = @"
run_id: $RunId
related_task_id: $RelatedTaskId
scenario: $Scenario
changed_files:
  - scripts/smoke-event-admin-permission-shift.ps1
  - git-ai/automation-route/runs/$RunId.md
key_changes:
  - Added reusable permission smoke for EVENT_ADMIN cross-event edit, unbind permission shift, and project write boundary checks.
  - Reused SCHOOL_ADMIN/EVENT_ADMIN/USER business assets and included cleanup for temporary events.
test_results:
  - overall: $($status.overall)
  - steps:
      school_admin_login: $($status.school_admin_login)
      event_admin_login: $($status.event_admin_login)
      user_login: $($status.user_login)
      scenario_main: $($status.scenario_main)
      cleanup: $($status.cleanup)
artifacts:
  - event_ids: "$eventIdsText"
  - project_ids: "$projectIdsText"
issue_inputs:
  - source: local-run
    signal: "$escapedError"
next_action: "Reuse this smoke after event-admin permission, bind/unbind, or event/project write-access changes."
"@
    Set-Content -Path $runPath -Value $content -Encoding UTF8
    Write-Step "Run record written: $runPath"
}

if ($status.overall -eq "pass") {
    Write-Host "=== Event Admin Permission Shift Smoke Completed Successfully ==="
}
