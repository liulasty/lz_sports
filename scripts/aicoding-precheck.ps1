param(
    [switch]$SkipCursorSync,
    [switch]$DryRunSync
)

$ErrorActionPreference = "Stop"

function Write-Step {
    param([string]$Message)
    Write-Host "[AICODING-PRECHECK] $Message" -ForegroundColor Cyan
}

function Assert-PathExists {
    param([string]$Path)
    if (-not (Test-Path $Path)) {
        throw "Required path not found: $Path"
    }
}

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $repoRoot

Write-Step "Repo root: $repoRoot"

$branch = (git branch --show-current).Trim()
if ($branch -ne "git-ai/automation-route") {
    throw "Current branch must be git-ai/automation-route, got: $branch"
}
Write-Step "Branch check passed: $branch"

$requiredDocs = @(
    "PROJECT_LOOP.md",
    "git-ai/automation-route/BACKLOG.md",
    "git-ai/automation-route/WORKFLOW_OPEN_CLOSE_SMOKE.md",
    "git-ai/automation-route/BUSINESS_STATUS.md",
    "git-ai/automation-route/FIRST_BATCH_AUTOMATION_TASKS.md"
)

foreach ($doc in $requiredDocs) {
    Assert-PathExists (Join-Path $repoRoot $doc)
}
Write-Step "Required docs check passed"

if (-not $SkipCursorSync) {
    $syncScript = Join-Path $repoRoot "scripts/sync-local-cursor.ps1"
    Assert-PathExists $syncScript
    if ($DryRunSync) {
        Write-Step "Running cursor sync dry-run (toWorktree)"
        powershell -ExecutionPolicy Bypass -File $syncScript -Direction toWorktree -DryRun
    } else {
        Write-Step "Running cursor sync (toWorktree)"
        powershell -ExecutionPolicy Bypass -File $syncScript -Direction toWorktree
    }
} else {
    Write-Step "Skip cursor sync by flag"
}

$gitStatus = git status --short
if ([string]::IsNullOrWhiteSpace($gitStatus)) {
    Write-Step "Working tree clean"
} else {
    Write-Step "Working tree has changes (allowed, review before commit)"
    Write-Host $gitStatus -ForegroundColor Yellow
}

Write-Step "Precheck completed. Ready for AICoding loop."
