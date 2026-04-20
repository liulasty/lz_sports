param(
    [string]$BackendUrl = "http://localhost:8080",
    [string]$Username = "smoke_user_447613714",
    [string]$Password = "Password123",
    [string]$AccessToken = "",
    [string]$AdminUsername = "event_admin_smoke",
    [string]$AdminPassword = "Password123",
    [switch]$SkipAuthNegative,
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

function Invoke-Step {
    param(
        [string]$Name,
        [scriptblock]$Action
    )
    Write-Host "[NOTIFY-SMOKE] $Name"
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
    param([object]$RespData)
    if ($null -eq $RespData) { return $null }
    if ($RespData -is [string]) { return $RespData }
    if ($RespData.PSObject -and $RespData.PSObject.Properties.Name -contains "token") {
        return $RespData.token
    }
    return $null
}

Write-Host "=== LZ Sports Notification Smoke ==="
Write-Host "Backend : $BackendUrl"
Write-Host "DryRun  : $DryRun"

$token = $AccessToken
if (-not $token) {
    $token = Invoke-Step -Name "Login as athlete user" -Action {
        $body = @{ username = $Username; password = $Password } | ConvertTo-Json
        $resp = Invoke-RestMethod -Uri "$BackendUrl/api/auth/login" -Method Post -ContentType "application/json" -Body $body -TimeoutSec 10
        Assert-ApiSuccess -Name "athlete-login" -Resp $resp
        $resolved = Resolve-AccessToken -RespData $resp.data
        if (-not $resolved) { throw "athlete-login failed: missing token" }
        return $resolved
    }
}

if (-not $DryRun) {
    $headers = @{ Authorization = "Bearer $token" }

    $all = Invoke-Step -Name "Notification page all" -Action {
        Invoke-RestMethod -Uri "$BackendUrl/api/notification/page?currentPage=1&pageSize=50" -Method Get -Headers $headers -TimeoutSec 10
    }
    Assert-ApiSuccess -Name "notification-page-all" -Resp $all

    $read = Invoke-Step -Name "Notification page read=true" -Action {
        Invoke-RestMethod -Uri "$BackendUrl/api/notification/page?currentPage=1&pageSize=50&isRead=true" -Method Get -Headers $headers -TimeoutSec 10
    }
    Assert-ApiSuccess -Name "notification-page-read" -Resp $read

    $unread = Invoke-Step -Name "Notification page read=false" -Action {
        Invoke-RestMethod -Uri "$BackendUrl/api/notification/page?currentPage=1&pageSize=50&isRead=false" -Method Get -Headers $headers -TimeoutSec 10
    }
    Assert-ApiSuccess -Name "notification-page-unread" -Resp $unread

    $unreadCount = Invoke-Step -Name "Unread count" -Action {
        Invoke-RestMethod -Uri "$BackendUrl/api/notification/unread-count" -Method Get -Headers $headers -TimeoutSec 10
    }
    Assert-ApiSuccess -Name "notification-unread-count" -Resp $unreadCount

    if (($read.data.total + $unread.data.total) -ne $all.data.total) {
        throw "pagination consistency failed: read($($read.data.total))+unread($($unread.data.total)) != all($($all.data.total))"
    }
    if ($unreadCount.data -ne $unread.data.total) {
        throw "unread count mismatch: unreadCount($($unreadCount.data)) != unreadTotal($($unread.data.total))"
    }

    if (-not $SkipAuthNegative) {
        Invoke-Step -Name "Auth negative: no token unread-count should be 401" -Action {
            try {
                Invoke-RestMethod -Uri "$BackendUrl/api/notification/unread-count" -Method Get -TimeoutSec 10 | Out-Null
                throw "expected 401 but request succeeded"
            } catch {
                $raw = $_.ErrorDetails.Message
                if (-not $raw -or $raw -notmatch '"code"\s*:\s*401') {
                    throw "expected 401 response for no-token unread-count"
                }
            }
        } | Out-Null
    }

    if ($AdminUsername -and $AdminPassword -and $all.data.records.Count -gt 0) {
        $adminToken = Invoke-Step -Name "Login as admin user" -Action {
            $adminBody = @{ username = $AdminUsername; password = $AdminPassword } | ConvertTo-Json
            $resp = Invoke-RestMethod -Uri "$BackendUrl/api/auth/login" -Method Post -ContentType "application/json" -Body $adminBody -TimeoutSec 10
            Assert-ApiSuccess -Name "admin-login" -Resp $resp
            $resolved = Resolve-AccessToken -RespData $resp.data
            if (-not $resolved) { throw "admin-login failed: missing token" }
            return $resolved
        }
        $adminHeaders = @{ Authorization = "Bearer $adminToken" }

        $targetId = $all.data.records[0].id
        Invoke-Step -Name "Cross-user read should be denied (403)" -Action {
            $resp = Invoke-RestMethod -Uri "$BackendUrl/api/notification/read/$targetId" -Method Put -Headers $adminHeaders -TimeoutSec 10
            if ($resp.code -ne 403) {
                throw "expected business 403 for cross-user read, got code=$($resp.code)"
            }
        } | Out-Null
    }
}

Write-Host "=== Notification Smoke Completed Successfully ==="
