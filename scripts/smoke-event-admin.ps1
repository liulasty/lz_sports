param(
    [string]$BackendUrl = "http://localhost:8080",
    [string]$AccountsFile = "git-ai/automation-route/accounts/business-accounts-latest.json",
    [string]$SchoolAdminUsername = "init_school_admin_01",
    [string]$SchoolAdminPassword = "Admin12345",
    [string]$RunId = "",
    [string]$RelatedTaskId = "AUTO-040",
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$runFolder = Join-Path $root "git-ai/automation-route/runs"

function Write-Step {
    param([string]$Msg)
    Write-Host "[EVENT-ADMIN] $Msg"
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
        username = $Username
        password = $Password
        role = [string]$resp.data.role
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

    throw "Unable to resolve a working SCHOOL_ADMIN account for event-admin smoke."
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

function Resolve-AbsolutePath {
    param([string]$Path)
    if ([System.IO.Path]::IsPathRooted($Path)) {
        return $Path
    }
    return Join-Path $root $Path
}

function New-DateString {
    param([datetime]$Value)
    return $Value.ToString("yyyy-MM-dd HH:mm:ss")
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
    $RunId = (Get-Date -Format "yyyy-MM-dd") + "-auto-040-event-admin"
}

$status = [ordered]@{
    school_admin_login = "pending"
    event_admin_login = "pending"
    athlete_draft_hidden = "pending"
    create_event = "pending"
    bind_admin = "pending"
    admin_list = "pending"
    publish_open = "pending"
    athlete_open_visible = "pending"
    withdraw_rejected = "pending"
    withdraw_cleanup = "pending"
    delete_cleanup = "pending"
    overall = "pending"
    error = ""
}

$createdEventId = $null
$createdEventName = $null
$schoolHeaders = $null
$athleteHeaders = $null
$cleanupDeleteEligible = $false

Write-Host "=== LZ Sports Event Admin Smoke ==="
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
    $athleteSeed = @($accounts.accounts | Where-Object { $_.role -eq "ATHLETE" } | Select-Object -First 1)
    if (-not $eventAdminSeed -or -not $athleteSeed) {
        throw "accounts file does not contain EVENT_ADMIN / ATHLETE"
    }

    Write-Step "Resolve SCHOOL_ADMIN"
    $schoolAdmin = Resolve-SchoolAdminLogin
    $schoolHeaders = @{ Authorization = "Bearer $($schoolAdmin.token)" }
    $status.school_admin_login = "pass"

    Write-Step "Login EVENT_ADMIN and ATHLETE seeded accounts"
    $eventAdmin = Login-Account -Username ([string]$eventAdminSeed.username) -Password ([string]$eventAdminSeed.password)
    $athlete = Login-Account -Username ([string]$athleteSeed.username) -Password ([string]$athleteSeed.password)
    $athleteHeaders = @{ Authorization = "Bearer $($athlete.token)" }
    $status.event_admin_login = "pass"

    $now = Get-Date
    $eventStart = $now.AddDays(3)
    $eventEnd = $now.AddDays(4)
    $projectStart = $eventStart.AddHours(1)
    $projectEnd = $projectStart.AddHours(2)
    $createdEventName = "AUTO-040 Event Lifecycle " + (Get-Date -Format "HHmmss")

    $body = @{
        name = $createdEventName
        type = "AUTO-040 smoke"
        fee = "0"
        maxItemsPerAthlete = 1
        registrationStartTime = New-DateString -Value $now.AddHours(-2)
        registrationEndTime = New-DateString -Value $now.AddDays(1)
        eventStartTime = New-DateString -Value $eventStart
        eventEndTime = New-DateString -Value $eventEnd
        projects = @(
            @{
                name = "AUTO-040 Project " + (Get-Date -Format "HHmmss")
                category = "CUSTOM"
                limitation = "ALL"
                maxAttendance = 10
                startTime = New-DateString -Value $projectStart
                endTime = New-DateString -Value $projectEnd
            }
        )
    }

    Write-Step "Create DRAFT event with one project"
    $createResp = Api-Post -Path "/api/event" -Headers $schoolHeaders -Body $body
    Assert-Code -Name "create-event" -Resp $createResp -Allowed @(200)
    $createdEventId = [long]$createResp.data
    $status.create_event = "pass"

    Write-Step "ATHLETE should not access DRAFT event detail"
    $draftVisible = Try-Login -Username ([string]$athleteSeed.username) -Password ([string]$athleteSeed.password) | Out-Null
    $draftResp = $null
    try {
        $draftResp = Api-Get -Path "/api/event/$createdEventId" -Headers $athleteHeaders
    } catch {
        $raw = $_.ErrorDetails.Message
        if (-not [string]::IsNullOrWhiteSpace($raw)) {
            $draftResp = $raw | ConvertFrom-Json
        } else {
            throw
        }
    }
    Assert-Code -Name "draft-hidden-from-athlete" -Resp $draftResp -Allowed @(404)
    $status.athlete_draft_hidden = "pass"

    Write-Step "Bind seeded EVENT_ADMIN to new event"
    $bindResp = Api-Post -Path "/api/admin/events/$createdEventId/admins" -Headers $schoolHeaders -Body @([long]$eventAdmin.id)
    Assert-Code -Name "bind-event-admin" -Resp $bindResp -Allowed @(200)
    $status.bind_admin = "pass"

    Write-Step "Admin list should include seeded EVENT_ADMIN"
    $adminListResp = Api-Get -Path "/api/admin/events/$createdEventId/admins" -Headers $schoolHeaders
    Assert-Code -Name "event-admin-list" -Resp $adminListResp -Allowed @(200)
    $adminIds = @($adminListResp.data | ForEach-Object { [long]$_.id })
    if ($adminIds -notcontains [long]$eventAdmin.id) {
        throw "event-admin-list failed: missing seeded EVENT_ADMIN id=$($eventAdmin.id)"
    }
    $status.admin_list = "pass"

    Write-Step "Publish event to OPEN"
    $publishResp = Api-Put -Path "/api/event/$createdEventId/status?status=OPEN" -Headers $schoolHeaders -Body $null
    Assert-Code -Name "event-open" -Resp $publishResp -Allowed @(200)
    $status.publish_open = "pass"

    Write-Step "ATHLETE should access OPEN event detail"
    $openResp = Api-Get -Path "/api/event/$createdEventId" -Headers $athleteHeaders
    Assert-Code -Name "open-visible-to-athlete" -Resp $openResp -Allowed @(200)
    if (-not $openResp.data -or [string]$openResp.data.eventStatus -ne "OPEN") {
        throw "open-visible-to-athlete failed: eventStatus is not OPEN"
    }
    $status.athlete_open_visible = "pass"

    Write-Step "Withdraw should be rejected after registration starts"
    $withdrawResp = $null
    try {
        $withdrawResp = Api-Post -Path "/api/admin/events/$createdEventId/withdraw" -Headers $schoolHeaders -Body $null
    } catch {
        $raw = $_.ErrorDetails.Message
        if (-not [string]::IsNullOrWhiteSpace($raw)) {
            $withdrawResp = $raw | ConvertFrom-Json
        } else {
            throw
        }
    }
    Assert-Code -Name "withdraw-after-reg-start" -Resp $withdrawResp -Allowed @(409)
    $status.withdraw_rejected = "pass"

    Write-Step "Move registration window to future and withdraw for cleanup"
    $cleanupBody = @{
        registrationStartTime = New-DateString -Value $now.AddDays(5)
        registrationEndTime = New-DateString -Value $now.AddDays(6)
        eventStartTime = New-DateString -Value $now.AddDays(7)
        eventEndTime = New-DateString -Value $now.AddDays(8)
    }
    $updateResp = Api-Put -Path "/api/event/$createdEventId" -Headers $schoolHeaders -Body $cleanupBody
    Assert-Code -Name "event-update-cleanup" -Resp $updateResp -Allowed @(200)
    $withdrawCleanupResp = Api-Post -Path "/api/admin/events/$createdEventId/withdraw" -Headers $schoolHeaders -Body $null
    Assert-Code -Name "withdraw-cleanup" -Resp $withdrawCleanupResp -Allowed @(200)
    $cleanupDeleteEligible = $true
    $status.withdraw_cleanup = "pass"

    Write-Step "Delete cleanup event"
    $deleteResp = Api-Delete -Path "/api/event/$createdEventId" -Headers $schoolHeaders
    Assert-Code -Name "delete-cleanup" -Resp $deleteResp -Allowed @(200)
    $cleanupDeleteEligible = $false
    $status.delete_cleanup = "pass"

    $status.overall = "pass"
} catch {
    $status.overall = "fail"
    $status.error = $_.Exception.Message
    throw
} finally {
    if ($cleanupDeleteEligible -and $createdEventId -and $schoolHeaders) {
        try {
            Write-Step "Cleanup leftover DRAFT event"
            $deleteResp = Api-Delete -Path "/api/event/$createdEventId" -Headers $schoolHeaders
            Assert-Code -Name "delete-cleanup-finally" -Resp $deleteResp -Allowed @(200)
            $status.delete_cleanup = "pass"
        } catch {
            Write-Step "Cleanup leftover event failed: $($_.Exception.Message)"
        }
    }

    if (-not (Test-Path $runFolder)) {
        New-Item -ItemType Directory -Path $runFolder | Out-Null
    }
    $runPath = Join-Path $runFolder "$RunId.md"
    $escapedError = $status.error -replace "\r?\n", " "
    if ([string]::IsNullOrWhiteSpace($escapedError)) { $escapedError = "none" }
    $eventIdText = "none"
    if ($createdEventId) { $eventIdText = [string]$createdEventId }
    $eventNameText = "none"
    if ($createdEventName) { $eventNameText = $createdEventName }
    $content = @"
run_id: $RunId
related_task_id: $RelatedTaskId
changed_files:
  - scripts/smoke-event-admin.ps1
  - git-ai/automation-route/runs/$RunId.md
key_changes:
  - Added a targeted event-admin smoke that creates a DRAFT event with one project, binds a seeded EVENT_ADMIN, publishes to OPEN, checks athlete visibility, verifies withdraw rejection after registration starts, then performs cleanup.
  - The smoke reuses the latest business role asset file and SCHOOL_ADMIN seed resolution so the lifecycle can be replayed without rebuilding the environment.
test_results:
  - overall: $($status.overall)
  - steps:
      school_admin_login: $($status.school_admin_login)
      event_admin_login: $($status.event_admin_login)
      create_event: $($status.create_event)
      athlete_draft_hidden: $($status.athlete_draft_hidden)
      bind_admin: $($status.bind_admin)
      admin_list: $($status.admin_list)
      publish_open: $($status.publish_open)
      athlete_open_visible: $($status.athlete_open_visible)
      withdraw_rejected: $($status.withdraw_rejected)
      withdraw_cleanup: $($status.withdraw_cleanup)
      delete_cleanup: $($status.delete_cleanup)
issue_inputs:
  - source: local-run
    signal: "$escapedError"
artifacts:
  - created_event_id: "$eventIdText"
  - created_event_name: "$eventNameText"
repeated_failures:
  - issue_key: event-admin-smoke
    attempts: 1
    blocked: false
next_task: "AUTO-041 扩展业务回归到成绩录入与发布最小闭环"
next_action: "Reuse this smoke after any event or event-admin permission/status change."
"@
    Set-Content -Path $runPath -Value $content -Encoding UTF8
    Write-Step "Run record written: $runPath"
}

if ($status.overall -eq "pass") {
    Write-Host "=== Event Admin Smoke Completed Successfully ==="
}
