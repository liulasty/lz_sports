param(
    [string]$EnvFile = "config/.env.dev",
    [int]$BackendPort = 8080,
    [int]$FrontendPort = 5173,
    [switch]$SkipPortCleanup,
    [switch]$SkipFrontend,
    [switch]$SkipBackend,
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

function Write-Step {
    param([string]$Msg)
    Write-Host "[DEV-START] $Msg"
}

function Get-CurrentBranch {
    return (git branch --show-current 2>$null).Trim()
}

function Read-EnvMap {
    param([string]$Path)
    $map = @{}
    if (-not (Test-Path $Path)) {
        throw "Env file not found: $Path"
    }
    Get-Content -Path $Path | ForEach-Object {
        $line = $_.Trim()
        if (-not $line -or $line.StartsWith("#")) { return }
        $idx = $line.IndexOf("=")
        if ($idx -lt 1) { return }
        $k = $line.Substring(0, $idx).Trim()
        $v = $line.Substring($idx + 1).Trim()
        $map[$k] = $v
    }
    return $map
}

function Set-ProcessEnv {
    param([hashtable]$Map)
    foreach ($k in $Map.Keys) {
        [Environment]::SetEnvironmentVariable($k, $Map[$k], "Process")
    }
}

function Stop-PortListeners {
    param([int[]]$Ports)
    foreach ($p in $Ports) {
        $lines = netstat -ano | Select-String -Pattern "LISTENING" | Select-String -Pattern ":$p\s"
        if (-not $lines) { continue }
        $killed = New-Object System.Collections.Generic.HashSet[string]
        foreach ($line in $lines) {
            $parts = ($line.ToString().Trim() -split "\s+")
            if ($parts.Length -ge 5) {
                $targetPid = $parts[-1]
                if ($targetPid -match "^\d+$" -and $killed.Add($targetPid)) {
                    Write-Step "Killing PID=$targetPid on port $p"
                    taskkill /PID $targetPid /F | Out-Null
                }
            }
        }
    }
}

function Start-Frontend {
    param([string]$RepoRoot, [int]$Port)
    $frontendDir = Join-Path $RepoRoot "lz_sports_frontend"
    $cmd = "cd '$frontendDir'; npm run dev -- --host --port $Port"
    Start-Process powershell -ArgumentList "-NoExit", "-NoProfile", "-Command", $cmd | Out-Null
}

function Start-Backend {
    param([string]$RepoRoot)
    $backendDir = Join-Path $RepoRoot "lz_sports_backend"
    $envKeys = @(
        "DB_URL","DB_USERNAME","DB_PASSWORD",
        "REDIS_HOST","REDIS_PORT","REDIS_DB","REDIS_PASSWORD",
        "JWT_SECRET","MAIL_USERNAME","MAIL_PASSWORD",
        "OSS_ACCESS_KEY_ID","OSS_ACCESS_KEY_SECRET",
        "SECURITY_USER_NAME","SECURITY_USER_PASSWORD"
    )
    $envAssign = @()
    foreach ($k in $envKeys) {
        $v = [Environment]::GetEnvironmentVariable($k, "Process")
        if ($null -ne $v) {
            $escaped = $v.Replace("'", "''")
            $envAssign += "`$env:$k='$escaped'"
        }
    }
    $prefix = ($envAssign -join "; ")
    $cmd = "$prefix; cd '$backendDir'; mvn spring-boot:run"
    Start-Process powershell -ArgumentList "-NoExit", "-NoProfile", "-Command", $cmd | Out-Null
}

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
Set-Location $repoRoot

Write-Host "=== LZ Sports Dev Start ==="
Write-Host "Repo        : $repoRoot"
Write-Host "EnvFile     : $EnvFile"
Write-Host "BackendPort : $BackendPort"
Write-Host "FrontendPort: $FrontendPort"
Write-Host "DryRun      : $DryRun"

$branch = Get-CurrentBranch
if ($branch -ne "git-ai/automation-route") {
    throw "Current branch is '$branch'. Please switch to git-ai/automation-route before dev-start."
}

$envMap = Read-EnvMap -Path (Join-Path $repoRoot $EnvFile)

if ($envMap.ContainsKey("DB_USER") -and -not $envMap.ContainsKey("DB_USERNAME")) {
    $envMap["DB_USERNAME"] = $envMap["DB_USER"]
}
if ($envMap.ContainsKey("DB_URL") -and $envMap["DB_URL"] -match "://mysql:") {
    $envMap["DB_URL"] = $envMap["DB_URL"] -replace "://mysql:", "://localhost:"
}
if ($envMap.ContainsKey("REDIS_HOST") -and $envMap["REDIS_HOST"] -eq "redis") {
    $envMap["REDIS_HOST"] = "localhost"
}

Set-ProcessEnv -Map $envMap
Write-Step "Loaded env vars from $EnvFile"

if (-not $SkipPortCleanup) {
    if ($DryRun) {
        Write-Step "DryRun: would cleanup ports $BackendPort,$FrontendPort"
    } else {
        Stop-PortListeners -Ports @($BackendPort, $FrontendPort)
    }
}

if (-not $SkipFrontend) {
    if ($DryRun) {
        Write-Step "DryRun: would start frontend on port $FrontendPort"
    } else {
        Start-Frontend -RepoRoot $repoRoot -Port $FrontendPort
        Write-Step "Frontend start command launched"
    }
}

if (-not $SkipBackend) {
    if ($DryRun) {
        Write-Step "DryRun: would start backend on port $BackendPort"
    } else {
        Start-Backend -RepoRoot $repoRoot
        Write-Step "Backend start command launched"
    }
}

Write-Host "=== Dev Start Script Completed ==="

