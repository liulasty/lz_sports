param(
    [ValidateSet("toWorktree", "toMain")]
    [string]$Direction = "toWorktree",
    [switch]$DryRun,
    [string]$MainRoot = "D:\soft\lz_sports",
    [string]$WorktreeRoot = "D:\soft\lz_sports_git_ai"
)

$ErrorActionPreference = "Stop"

$mainCursor = Join-Path $MainRoot ".cursor"
$worktreeCursor = Join-Path $WorktreeRoot ".cursor"

if (-not (Test-Path $mainCursor)) {
    throw "Main .cursor path not found: $mainCursor"
}
if (-not (Test-Path $worktreeCursor)) {
    throw "Worktree .cursor path not found: $worktreeCursor"
}

if ($Direction -eq "toWorktree") {
    $source = $mainCursor
    $target = $worktreeCursor
} else {
    $source = $worktreeCursor
    $target = $mainCursor
}

$args = @(
    "`"$source`"",
    "`"$target`"",
    "/MIR",
    "/XD", ".git",
    "/R:2",
    "/W:1",
    "/NFL",
    "/NDL",
    "/NP"
)

if ($DryRun) {
    $args += "/L"
}

Write-Host "Sync direction: $Direction"
Write-Host "Source: $source"
Write-Host "Target: $target"

& robocopy @args | Out-Host
$code = $LASTEXITCODE

# robocopy exit codes: 0-7 success, 8+ failure
if ($code -ge 8) {
    throw "robocopy failed with exit code $code"
}

Write-Host "Sync completed with robocopy exit code: $code"
