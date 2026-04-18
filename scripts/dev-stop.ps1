param(
    [int]$BackendPort = 8080,
    [int]$FrontendPort = 5173,
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

function Write-Step {
    param([string]$Msg)
    Write-Host "[DEV-STOP] $Msg"
}

function Stop-PortListeners {
    param([int[]]$Ports)
    foreach ($p in $Ports) {
        $lines = netstat -ano | Select-String -Pattern "LISTENING" | Select-String -Pattern ":$p\s"
        if (-not $lines) {
            Write-Step "No LISTENING process on port $p"
            continue
        }
        $killed = New-Object System.Collections.Generic.HashSet[string]
        foreach ($line in $lines) {
            $parts = ($line.ToString().Trim() -split "\s+")
            if ($parts.Length -ge 5) {
                $targetPid = $parts[-1]
                if ($targetPid -match "^\d+$" -and $killed.Add($targetPid)) {
                    if ($DryRun) {
                        Write-Step "DryRun: would kill PID=$targetPid on port $p"
                    } else {
                        Write-Step "Killing PID=$targetPid on port $p"
                        taskkill /PID $targetPid /F | Out-Null
                    }
                }
            }
        }
    }
}

Write-Host "=== LZ Sports Dev Stop ==="
Write-Host "BackendPort : $BackendPort"
Write-Host "FrontendPort: $FrontendPort"
Write-Host "DryRun      : $DryRun"

Stop-PortListeners -Ports @($BackendPort, $FrontendPort)

Write-Host "=== Dev Stop Script Completed ==="

