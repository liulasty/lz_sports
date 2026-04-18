param(
    [string]$FrontendUrl = "http://localhost:5173",
    [string]$BackendUrl = "http://localhost:8080",
    [string]$Username = "smoke_user_447613714",
    [string]$Password = "Password123",
    [string]$AccessToken = "",
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$linkupScript = Join-Path $root "scripts/smoke-linkup.ps1"
$notifyScript = Join-Path $root "scripts/smoke-notification.ps1"

Write-Host "=== LZ Sports Full Smoke ==="
Write-Host "Frontend: $FrontendUrl"
Write-Host "Backend : $BackendUrl"
Write-Host "DryRun  : $DryRun"

$linkupArgs = @(
    "-ExecutionPolicy", "Bypass",
    "-File", $linkupScript,
    "-FrontendUrl", $FrontendUrl,
    "-BackendUrl", $BackendUrl,
    "-Username", $Username,
    "-Password", $Password
)

if ($AccessToken) {
    $linkupArgs += @("-AccessToken", $AccessToken)
}
if ($DryRun) {
    $linkupArgs += "-DryRun"
}

Write-Host "[FULL-SMOKE] Run linkup smoke"
powershell @linkupArgs
if ($LASTEXITCODE -ne 0) {
    throw "linkup smoke failed with exit code $LASTEXITCODE"
}

$notifyArgs = @(
    "-ExecutionPolicy", "Bypass",
    "-File", $notifyScript,
    "-BackendUrl", $BackendUrl,
    "-Username", $Username,
    "-Password", $Password
)

if ($AccessToken) {
    $notifyArgs += @("-AccessToken", $AccessToken)
}
if ($DryRun) {
    $notifyArgs += "-DryRun"
}

Write-Host "[FULL-SMOKE] Run notification smoke"
powershell @notifyArgs
if ($LASTEXITCODE -ne 0) {
    throw "notification smoke failed with exit code $LASTEXITCODE"
}

Write-Host "=== Full Smoke Completed Successfully ==="
