param(
    [string]$BackendUrl = "http://localhost:8080",
    [string]$Username = "autou_164656_10",
    [string]$Email = "auto16465610@qq.com",
    [string]$OriginalPassword = "Pass12345",
    [string]$TemporaryPassword = "Pass55667",
    [string]$BackendLogPath = "C:\Users\Administrator\.cursor\projects\d-soft-lz-sports\terminals\543545.txt",
    [string]$RunId = "",
    [string]$RelatedTaskId = "AUTO-034",
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$runFolder = Join-Path $root "git-ai/automation-route/runs"

function Write-Step {
    param([string]$Msg)
    Write-Host "[FORGOT-ONCE] $Msg"
}

function Invoke-Login {
    param([string]$User, [string]$Password)
    $body = @{ username = $User; password = $Password } | ConvertTo-Json
    try {
        return Invoke-RestMethod -Uri "$BackendUrl/api/auth/login" -Method Post -ContentType "application/json" -Body $body -TimeoutSec 10
    } catch {
        if ($_.ErrorDetails -and $_.ErrorDetails.Message) {
            return ($_.ErrorDetails.Message | ConvertFrom-Json)
        }
        throw
    }
}

function Extract-CodeFromLog {
    param([string]$VerifyToken, [string]$TargetEmail)
    $line = Select-String -Path $BackendLogPath -Pattern "verify_token:$VerifyToken -> (\d{6}):$([regex]::Escape($TargetEmail))" | Select-Object -Last 1
    if (-not $line) { return $null }
    $m = [regex]::Match($line.Line, "-> (\d{6}):")
    if (-not $m.Success) { return $null }
    return $m.Groups[1].Value
}

function Assert-Code {
    param([string]$Name, [int]$Actual, [int[]]$Allowed)
    if (-not ($Allowed -contains $Actual)) {
        throw "$Name failed: expected [$($Allowed -join ',')], got $Actual"
    }
}

if (-not $RunId) {
    $RunId = (Get-Date -Format "yyyy-MM-dd") + "-auto-034-forgot-once"
}

$status = @{
    backend_health = "pending"
    send_code = "pending"
    verify_code = "pending"
    reset_once = "pending"
    reset_reuse_block = "pending"
    login_old_blocked = "pending"
    login_new_ok = "pending"
    restore_password = "pending"
    overall = "pending"
    error = ""
}

$captured = @{
    verify_token = ""
    verify_code = ""
    reset_token = ""
}

Write-Host "=== LZ Sports Forgot-Password Once Smoke ==="
Write-Host "RunId      : $RunId"
Write-Host "BackendUrl : $BackendUrl"
Write-Host "Username   : $Username"
Write-Host "Email      : $Email"
Write-Host "DryRun     : $DryRun"

try {
    Write-Step "Backend health check"
    if (-not $DryRun) {
        $init = Invoke-RestMethod -Uri "$BackendUrl/api/system/init-status" -Method Get -TimeoutSec 8
        Assert-Code -Name "init-status" -Actual ([int]$init.code) -Allowed @(200)
        $status.backend_health = "pass"
    } else {
        $status.backend_health = "skipped"
    }

    if ($DryRun) {
        $status.send_code = "skipped"
        $status.verify_code = "skipped"
        $status.reset_once = "skipped"
        $status.reset_reuse_block = "skipped"
        $status.login_old_blocked = "skipped"
        $status.login_new_ok = "skipped"
        $status.restore_password = "skipped"
        $status.overall = "pass"
    } else {
        Write-Step "Send verify code"
        $send = Invoke-RestMethod -Uri "$BackendUrl/api/auth/send-verify-code?email=$Email" -Method Post -TimeoutSec 10
        Assert-Code -Name "send-verify-code" -Actual ([int]$send.code) -Allowed @(200)
        $captured.verify_token = [string]$send.data
        $status.send_code = "pass"

        Write-Step "Extract code from backend log"
        $code = $null
        for ($i = 0; $i -lt 40 -and -not $code; $i++) {
            Start-Sleep -Milliseconds 300
            $code = Extract-CodeFromLog -VerifyToken $captured.verify_token -TargetEmail $Email
        }
        if (-not $code) {
            throw "Unable to extract verify code from backend log"
        }
        $captured.verify_code = $code

        Write-Step "Verify code to obtain reset token"
        $verify = Invoke-RestMethod -Uri "$BackendUrl/api/auth/verify-code?verifyToken=$($captured.verify_token)&code=$($captured.verify_code)" -Method Post -TimeoutSec 10
        Assert-Code -Name "verify-code" -Actual ([int]$verify.code) -Allowed @(200)
        $captured.reset_token = [string]$verify.data
        $status.verify_code = "pass"

        $payload = @{
            email = $Email
            newPassword = $TemporaryPassword
            verifyToken = $captured.reset_token
        } | ConvertTo-Json

        Write-Step "Reset password first time"
        $reset1 = Invoke-RestMethod -Uri "$BackendUrl/api/auth/reset-password" -Method Post -ContentType "application/json" -Body $payload -TimeoutSec 10
        Assert-Code -Name "reset-password-first" -Actual ([int]$reset1.code) -Allowed @(200)
        $status.reset_once = "pass"

        Write-Step "Reuse same reset token (should fail)"
        $reset2 = $null
        try {
            $reset2 = Invoke-RestMethod -Uri "$BackendUrl/api/auth/reset-password" -Method Post -ContentType "application/json" -Body $payload -TimeoutSec 10
        } catch {
            if ($_.ErrorDetails -and $_.ErrorDetails.Message) {
                $reset2 = $_.ErrorDetails.Message | ConvertFrom-Json
            } else {
                throw
            }
        }
        if ($null -eq $reset2) {
            throw "reset-password-reuse returned empty response"
        }
        Assert-Code -Name "reset-password-reuse" -Actual ([int]$reset2.code) -Allowed @(400)
        $status.reset_reuse_block = "pass"

        Write-Step "Login with old password should fail"
        $loginOld = Invoke-Login -User $Username -Password $OriginalPassword
        Assert-Code -Name "login-old-after-reset" -Actual ([int]$loginOld.code) -Allowed @(409)
        $status.login_old_blocked = "pass"

        Write-Step "Login with temporary password should pass"
        $loginNew = Invoke-Login -User $Username -Password $TemporaryPassword
        Assert-Code -Name "login-new-after-reset" -Actual ([int]$loginNew.code) -Allowed @(200)
        $status.login_new_ok = "pass"

        Write-Step "Restore original password"
        $restoreHeaders = @{ Authorization = "Bearer $($loginNew.data.token)" }
        $restoreBody = @{
            oldPassword = $TemporaryPassword
            newPassword = $OriginalPassword
        } | ConvertTo-Json
        $restore = Invoke-RestMethod -Uri "$BackendUrl/api/auth/update" -Method Post -Headers $restoreHeaders -ContentType "application/json" -Body $restoreBody -TimeoutSec 10
        Assert-Code -Name "restore-password" -Actual ([int]$restore.code) -Allowed @(200)
        $status.restore_password = "pass"

        $status.overall = "pass"
    }
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
    if ([string]::IsNullOrWhiteSpace($escapedError)) { $escapedError = "none" }

    $content = @"
run_id: $RunId
related_task_id: $RelatedTaskId
changed_files:
  - scripts/smoke-forgot-once.ps1
key_changes:
  - Added dedicated repeatable smoke for forgot-password token single-use chain.
test_results:
  - overall: $($status.overall)
  - steps:
      backend_health: $($status.backend_health)
      send_code: $($status.send_code)
      verify_code: $($status.verify_code)
      reset_once: $($status.reset_once)
      reset_reuse_block: $($status.reset_reuse_block)
      login_old_blocked: $($status.login_old_blocked)
      login_new_ok: $($status.login_new_ok)
      restore_password: $($status.restore_password)
  - inputs:
      username: "$Username"
      email: "$Email"
risks:
  - "Depends on backend log accessibility for extracting verification code in local runs."
issue_inputs:
  - source: local-run
    signal: "$escapedError"
token_source:
  - "reset token from /api/auth/verify-code"
repeated_failures:
  - issue_key: forgot-password-token-once
    attempts: 1
    blocked: false
next_action: "Run scripts/test.(bat|sh) forgot-once for repeat validation."
"@

    Set-Content -Path $runPath -Value $content -Encoding UTF8
    Write-Step "Run record written: $runPath"
}

if ($status.overall -eq "pass") {
    Write-Host "=== Forgot-Password Once Smoke Completed Successfully ==="
}
