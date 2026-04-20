param(
    [string]$BackendUrl = "http://localhost:8080",
    [string]$AccountsFile = "git-ai/automation-route/accounts/business-accounts-latest.json",
    [string]$SchoolAdminUsername = "init_school_admin_01",
    [string]$SchoolAdminPassword = "Admin12345",
    [string]$RunId = "",
    [string]$RelatedTaskId = "AUTO-049",
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$runFolder = Join-Path $root "git-ai/automation-route/runs"
. (Join-Path $PSScriptRoot "lib/business-code-assert.ps1")

function Write-Step {
    param([string]$Msg)
    Write-Host "[SCORE-TEMPLATE] $Msg"
}

function Resolve-AbsolutePath {
    param([string]$Path)
    if ([System.IO.Path]::IsPathRooted($Path)) { return $Path }
    return Join-Path $root $Path
}

function Login-Account {
    param([string]$Username, [string]$Password)
    $body = @{ username = $Username; password = $Password } | ConvertTo-Json
    $resp = Invoke-RestMethod -Uri "$BackendUrl/api/auth/login" -Method Post -ContentType "application/json" -Body $body -TimeoutSec 10
    if ([int]$resp.code -ne 200) {
        throw "login($Username) failed: code=$($resp.code), msg=$($resp.msg)"
    }
    return [PSCustomObject]@{
        id = [long]$resp.data.id
        username = $Username
        role = [string]$resp.data.role
        token = [string]$resp.data.token
    }
}

function Resolve-SchoolAdminLogin {
    $candidates = @(
        @{ username = $SchoolAdminUsername; password = $SchoolAdminPassword },
        @{ username = "init_school_admin_01"; password = "admin123" },
        @{ username = "init_school_admin_01"; password = "Admin12345" },
        @{ username = "init_school_admin_02"; password = "Admin12345" },
        @{ username = "init_school_admin_02"; password = "admin123" }
    )
    foreach ($candidate in $candidates) {
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

function Assert-ExcelResponse {
    param(
        [string]$Name,
        [string]$Path,
        [string]$Token
    )
    $resp = Invoke-WebRequest -Uri "$BackendUrl$Path" -Method Get -Headers @{ Authorization = "Bearer $Token" } -TimeoutSec 20
    $ct = [string]$resp.Headers["Content-Type"]
    if (-not $ct.ToLower().Contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
        throw "$Name failed: unexpected content-type=$ct"
    }
    if ([int]$resp.RawContentLength -le 0) {
        throw "$Name failed: empty file content"
    }
}

if (-not $RunId) {
    $RunId = (Get-Date -Format "yyyy-MM-dd") + "-auto-049-template-export"
}

$status = [ordered]@{
    school_admin_login = "pending"
    event_admin_login = "pending"
    user_login = "pending"
    athlete_login = "pending"
    mixed_candidate_prepare = "pending"
    template_export_event_admin = "pending"
    score_export_event_admin = "pending"
    registration_export_event_admin = "pending"
    template_export_user_forbidden = "pending"
    bad_import_rejected = "pending"
    missing_header_import_rejected = "pending"
    mixed_valid_invalid_import = "pending"
    overall = "pending"
    error = ""
}

Write-Host "=== LZ Sports Score Template/Export Smoke ==="
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
    $athleteSeed = @($accounts.accounts | Where-Object { $_.role -eq "ATHLETE" } | Select-Object -First 1)
    if (-not $eventAdminSeed -or -not $userSeed -or -not $athleteSeed) {
        throw "accounts file does not contain EVENT_ADMIN / USER / ATHLETE"
    }

    try {
        $null = Resolve-SchoolAdminLogin
        $status.school_admin_login = "pass"
    } catch {
        Write-Step "SCHOOL_ADMIN unavailable, continue with role assets only"
        $status.school_admin_login = "skipped"
    }
    $eventAdmin = Login-Account -Username ([string]$eventAdminSeed.username) -Password ([string]$eventAdminSeed.password)
    $user = Login-Account -Username ([string]$userSeed.username) -Password ([string]$userSeed.password)
    $athlete = Login-Account -Username ([string]$athleteSeed.username) -Password ([string]$athleteSeed.password)
    $status.event_admin_login = "pass"
    $status.user_login = "pass"
    $status.athlete_login = "pass"

    Write-Step "Prepare one mixed-import valid candidate registration"
    $hEvent = @{ Authorization = "Bearer $($eventAdmin.token)" }
    $hAthlete = @{ Authorization = "Bearer $($athlete.token)" }
    $projectResp = Invoke-RestMethod -Uri "$BackendUrl/api/project/event/1" -Method Get -Headers $hAthlete -TimeoutSec 12
    Assert-BusinessCode -Name "project-list-for-mixed" -Resp $projectResp -AllowedCodes @(200)
    $projectRows = @()
    if ($projectResp.data) { $projectRows = @($projectResp.data) }
    if ($projectRows.Count -eq 0) {
        throw "prepare-mixed-candidate failed: no project in event 1"
    }
    $appliedProjectId = $null
    foreach ($project in $projectRows) {
        try {
            $applyResp = Invoke-RestMethod -Uri "$BackendUrl/api/registration/apply/$($project.id)" -Method Post -Headers $hAthlete -TimeoutSec 12
            if ([int]$applyResp.code -eq 200) {
                $appliedProjectId = [long]$project.id
                break
            }
        } catch {
            continue
        }
    }
    if ($null -eq $appliedProjectId) {
        throw "prepare-mixed-candidate failed: no appliable project for athlete"
    }
    $targetRegistrationId = $null
    for ($i = 0; $i -lt 10; $i++) {
        Start-Sleep -Milliseconds 500
        $pendingResp = Invoke-RestMethod -Uri "$BackendUrl/api/registration/page?currentPage=1&pageSize=200&status=PENDING" -Method Get -Headers $hEvent -TimeoutSec 12
        Assert-BusinessCode -Name "registration-pending-for-mixed" -Resp $pendingResp -AllowedCodes @(200)
        $pendingRows = @()
        if ($pendingResp.data -and $pendingResp.data.records) { $pendingRows = @($pendingResp.data.records) }
        $hit = @($pendingRows | Where-Object { [long]$_.athleteId -eq [long]$athlete.id -and [long]$_.itemId -eq [long]$appliedProjectId } | Select-Object -First 1)
        if ($hit) {
            $targetRegistrationId = [long]$hit.id
            break
        }
    }
    if ($null -eq $targetRegistrationId) {
        throw "prepare-mixed-candidate failed: pending registration not found"
    }
    $approveResp = Invoke-RestMethod -Uri "$BackendUrl/api/registration/attend/${targetRegistrationId}?eventId=1" -Method Put -Headers $hEvent -ContentType "application/json" -Body "{}" -TimeoutSec 12
    Assert-BusinessCode -Name "registration-approve-for-mixed" -Resp $approveResp -AllowedCodes @(200)
    $status.mixed_candidate_prepare = "pass"

    Write-Step "EVENT_ADMIN template export should return excel"
    Assert-ExcelResponse -Name "score-template-export" -Path "/api/score/template/1" -Token $eventAdmin.token
    $status.template_export_event_admin = "pass"

    Write-Step "EVENT_ADMIN score export should return excel"
    Assert-ExcelResponse -Name "score-export" -Path "/api/score/export/1" -Token $eventAdmin.token
    $status.score_export_event_admin = "pass"

    Write-Step "EVENT_ADMIN registration export should return excel"
    Assert-ExcelResponse -Name "score-export-registration" -Path "/api/score/export-registration/1" -Token $eventAdmin.token
    $status.registration_export_event_admin = "pass"

    Write-Step "USER template export should be forbidden"
    $templateForbiddenResp = Invoke-WebRequest -Uri "$BackendUrl/api/score/template/1" -Method Get -Headers @{ Authorization = "Bearer $($user.token)" } -TimeoutSec 20
    $templateForbiddenJson = $templateForbiddenResp.Content | ConvertFrom-Json
    Assert-BusinessCode -Name "score-template-user-forbidden" -Resp $templateForbiddenJson -AllowedCodes @(403)
    $status.template_export_user_forbidden = "pass"

    Write-Step "Bad import file should be rejected"
    $badPath = Join-Path $runFolder "bad-import-$RunId.txt"
    if (-not (Test-Path $runFolder)) { New-Item -ItemType Directory -Path $runFolder | Out-Null }
    "not-an-excel" | Set-Content -Path $badPath -Encoding UTF8
    $raw = curl.exe -s -X POST "$BackendUrl/api/score/import/1" -H "Authorization: Bearer $($eventAdmin.token)" -F "file=@$badPath"
    $importResp = $raw | ConvertFrom-Json
    if ([int]$importResp.code -ne 400) {
        throw "score-import-bad-file failed: expected code=400, got code=$($importResp.code), msg=$($importResp.msg)"
    }
    Remove-Item $badPath -ErrorAction SilentlyContinue
    $status.bad_import_rejected = "pass"

    Write-Step "Missing-header import should be rejected"
    $templatePath = Join-Path $runFolder "template-$RunId.xlsx"
    $missingHeaderPath = Join-Path $runFolder "missing-header-$RunId.xlsx"
    $mixedPath = Join-Path $runFolder "mixed-import-$RunId.xlsx"
    Invoke-WebRequest -Uri "$BackendUrl/api/score/template/1" -Method Get -Headers @{ Authorization = "Bearer $($eventAdmin.token)" } -TimeoutSec 20 -OutFile $templatePath
    Copy-Item $templatePath $missingHeaderPath -Force
    Copy-Item $templatePath $mixedPath -Force

    $pythonScript = @'
import sys
from openpyxl import load_workbook

mode = sys.argv[1]
path = sys.argv[2]
target_registration_id = int(sys.argv[3]) if len(sys.argv) > 3 else None
wb = load_workbook(path)
ws = wb.worksheets[0]
headers = {str(cell.value): idx + 1 for idx, cell in enumerate(ws[1]) if cell.value is not None}
reg_col = headers.get("报名ID", 1)
score_col = headers.get("成绩", 5)
if mode == "missing_header":
    ws.cell(row=1, column=reg_col, value="报名ID_WRONG")
elif mode == "mixed":
    if ws.max_row < 2:
        raise RuntimeError("template has no data rows")
    if ws.cell(row=2, column=reg_col).value is None:
        raise RuntimeError("row2 has no registration id")
    if target_registration_id is not None:
        ws.cell(row=2, column=reg_col, value=target_registration_id)
    ws.cell(row=2, column=score_col, value="12.34")
    if ws.max_row < 3:
        ws.append([None] * ws.max_column)
    ws.cell(row=3, column=reg_col, value=ws.cell(row=2, column=reg_col).value)
    ws.cell(row=3, column=score_col, value="11.11")
else:
    raise RuntimeError("unsupported mode")
wb.save(path)
'@
    $pyPath = Join-Path $runFolder "mutate-import-$RunId.py"
    Set-Content -Path $pyPath -Value $pythonScript -Encoding UTF8
    python $pyPath missing_header $missingHeaderPath
    python $pyPath mixed $mixedPath $targetRegistrationId

    $rawMissing = curl.exe -s -X POST "$BackendUrl/api/score/import/1" -H "Authorization: Bearer $($eventAdmin.token)" -F "file=@$missingHeaderPath"
    $missingCodeMatch = [regex]::Match($rawMissing, '"code"\s*:\s*(\d+)')
    if (-not $missingCodeMatch.Success) {
        throw "score-import-missing-header failed: unable to parse business code from response"
    }
    $missingCode = [int]$missingCodeMatch.Groups[1].Value
    $missingFailMatch = [regex]::Match($rawMissing, '"failCount"\s*:\s*(\d+)')
    $missingFailCount = 0
    if ($missingFailMatch.Success) { $missingFailCount = [int]$missingFailMatch.Groups[1].Value }
    if ($missingCode -eq 200 -and $missingFailCount -eq 0) {
        throw "score-import-missing-header failed: expected rejection or failure rows, got pure success"
    }
    if (($missingCode -ne 400) -and ($missingCode -ne 200)) {
        throw "score-import-missing-header failed: unexpected code=$missingCode"
    }
    if ($missingCode -eq 200 -and $missingFailCount -le 0) {
        throw "score-import-missing-header failed: expected failCount>0 when code=200"
    }
    $status.missing_header_import_rejected = "pass"

    Write-Step "Mixed valid/invalid import should include both success and failure"
    $rawMixed = curl.exe -s -X POST "$BackendUrl/api/score/import/1" -H "Authorization: Bearer $($eventAdmin.token)" -F "file=@$mixedPath"
    $mixedCodeMatch = [regex]::Match($rawMixed, '"code"\s*:\s*(\d+)')
    $mixedSuccessMatch = [regex]::Match($rawMixed, '"successCount"\s*:\s*(\d+)')
    $mixedFailMatch = [regex]::Match($rawMixed, '"failCount"\s*:\s*(\d+)')
    if (-not $mixedCodeMatch.Success) {
        throw "score-import-mixed failed: unable to parse business code from response"
    }
    $mixedCode = [int]$mixedCodeMatch.Groups[1].Value
    $mixedSuccess = 0
    $mixedFail = 0
    if ($mixedSuccessMatch.Success) { $mixedSuccess = [int]$mixedSuccessMatch.Groups[1].Value }
    if ($mixedFailMatch.Success) { $mixedFail = [int]$mixedFailMatch.Groups[1].Value }
    if ($mixedCode -ne 200) {
        throw "score-import-mixed failed: expected code=200, got code=$mixedCode"
    }
    if ($mixedSuccess -le 0 -or $mixedFail -le 0) {
        throw "score-import-mixed failed: expected successCount>0 and failCount>0, got success=$mixedSuccess, fail=$mixedFail"
    }
    if ($rawMixed -notmatch '"failures"\s*:\s*\[') {
        throw "score-import-mixed failed: expected failure details"
    }
    $status.mixed_valid_invalid_import = "pass"

    Remove-Item $templatePath -ErrorAction SilentlyContinue
    Remove-Item $missingHeaderPath -ErrorAction SilentlyContinue
    Remove-Item $mixedPath -ErrorAction SilentlyContinue
    Remove-Item $pyPath -ErrorAction SilentlyContinue

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
  - scripts/smoke-score-template-export.ps1
  - git-ai/automation-route/runs/$RunId.md
key_changes:
  - Added dedicated score template/export smoke that validates template export, score export, registration export, and role boundary.
  - Added bad-file import negative assertion to ensure invalid file types are rejected with business code 400.
test_results:
  - overall: $($status.overall)
  - steps:
      school_admin_login: $($status.school_admin_login)
      event_admin_login: $($status.event_admin_login)
      user_login: $($status.user_login)
      athlete_login: $($status.athlete_login)
      mixed_candidate_prepare: $($status.mixed_candidate_prepare)
      template_export_event_admin: $($status.template_export_event_admin)
      score_export_event_admin: $($status.score_export_event_admin)
      registration_export_event_admin: $($status.registration_export_event_admin)
      template_export_user_forbidden: $($status.template_export_user_forbidden)
      bad_import_rejected: $($status.bad_import_rejected)
      missing_header_import_rejected: $($status.missing_header_import_rejected)
      mixed_valid_invalid_import: $($status.mixed_valid_invalid_import)
issue_inputs:
  - source: local-run
    signal: "$escapedError"
next_action: "Reuse this smoke after score import/export/template changes."
"@
    Set-Content -Path $runPath -Value $content -Encoding UTF8
    Write-Step "Run record written: $runPath"
}

if ($status.overall -eq "pass") {
    Write-Host "=== Score Template/Export Smoke Completed Successfully ==="
}
