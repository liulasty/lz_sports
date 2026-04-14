param(
    [string]$FrontendUrl = "http://localhost:5173",
    [string]$BackendUrl = "http://localhost:8080",
    [string]$Username = "admin",
    [string]$Password = "password",
    [string]$AccessToken = "",
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

function Invoke-Step {
    param(
        [string]$Name,
        [scriptblock]$Action
    )
    Write-Host "[SMOKE] $Name"
    if ($DryRun) {
        Write-Host "  -> DryRun mode: skipped"
        return $null
    }
    return & $Action
}

function Assert-ApiSuccess {
    param(
        [string]$Name,
        [object]$Resp
    )
    if ($null -eq $Resp) {
        throw "$Name failed: empty response"
    }
    if ($Resp.code -ne 200) {
        throw "$Name failed: code=$($Resp.code), msg=$($Resp.msg)"
    }
}

function Resolve-AccessToken {
    param(
        [object]$RespData
    )
    if ($null -eq $RespData) {
        return $null
    }
    if ($RespData -is [string]) {
        return $RespData
    }
    if ($RespData.PSObject -and $RespData.PSObject.Properties.Name -contains "token") {
        return $RespData.token
    }
    return $null
}

Write-Host "=== LZ Sports Smoke Linkup ==="
Write-Host "Frontend: $FrontendUrl"
Write-Host "Backend : $BackendUrl"
Write-Host "DryRun  : $DryRun"

Invoke-Step -Name "Frontend availability" -Action {
    $resp = Invoke-WebRequest -Uri $FrontendUrl -Method Get -TimeoutSec 8
    if ($resp.StatusCode -lt 200 -or $resp.StatusCode -ge 400) {
        throw "Frontend unavailable: HTTP $($resp.StatusCode)"
    }
}

Invoke-Step -Name "Backend init-status check" -Action {
    $resp = Invoke-RestMethod -Uri "$BackendUrl/api/system/init-status" -Method Get -TimeoutSec 8
    Assert-ApiSuccess -Name "init-status" -Resp $resp
}

$token = $AccessToken
if (-not $token) {
    $token = Invoke-Step -Name "Login (key path: 登录)" -Action {
        $loginBody = @{
            username = $Username
            password = $Password
        } | ConvertTo-Json

        $resp = Invoke-RestMethod -Uri "$BackendUrl/api/auth/login" -Method Post -ContentType "application/json" -Body $loginBody -TimeoutSec 10
        if ($resp.code -eq 409) {
            throw "login failed: business 409. Use -AccessToken to continue smoke replay."
        }
        Assert-ApiSuccess -Name "login" -Resp $resp
        $resolvedToken = Resolve-AccessToken -RespData $resp.data
        if (-not $resolvedToken) {
            throw "login failed: missing token in response data"
        }
        return $resolvedToken
    }
} else {
    Write-Host "[SMOKE] Login (key path: 登录)"
    Write-Host "  -> AccessToken provided: skipping login request"
}

if (-not $DryRun) {
    $headers = @{ Authorization = "Bearer $token" }

    Invoke-Step -Name "Registration query (key path: 报名)" -Action {
        $resp = Invoke-RestMethod -Uri "$BackendUrl/api/registration/my" -Method Get -Headers $headers -TimeoutSec 10
        Assert-ApiSuccess -Name "registration-my" -Resp $resp
    }

    Invoke-Step -Name "Score query (key path: 成绩)" -Action {
        $resp = Invoke-RestMethod -Uri "$BackendUrl/api/score/page?currentPage=1&pageSize=10" -Method Get -Headers $headers -TimeoutSec 10
        Assert-ApiSuccess -Name "score-page" -Resp $resp
    }
}

Write-Host "=== Smoke Completed Successfully ==="
