param(
    [string]$BackendUrl = "http://localhost:8080",
    [string]$AccountsFile = "git-ai/automation-route/accounts/business-accounts-latest.json",
    [string]$UserUsername = "",
    [string]$UserPassword = "",
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

function Invoke-Step {
    param(
        [string]$Name,
        [scriptblock]$Action
    )
    Write-Host "[DEPT-TREE] $Name"
    if ($DryRun) {
        Write-Host "  -> DryRun mode: skipped"
        return $null
    }
    return & $Action
}

function Assert-ApiCode {
    param(
        [string]$Name,
        [object]$Resp,
        [int[]]$AllowedCodes
    )
    if ($null -eq $Resp) { throw "$Name failed: empty response" }
    if (-not ($AllowedCodes -contains [int]$Resp.code)) {
        throw "$Name failed: expected code in [$($AllowedCodes -join ',')], got code=$($Resp.code), msg=$($Resp.msg)"
    }
}

function Resolve-UserLoginFromAccountsFile {
    if (-not $AccountsFile) { return $null }
    if (-not (Test-Path $AccountsFile)) { return $null }

    try {
        $json = Get-Content -Raw -Path $AccountsFile | ConvertFrom-Json
        foreach ($acc in $json.accounts) {
            if ($acc.role -eq "USER" -and $acc.username -and $acc.password) {
                return [PSCustomObject]@{
                    username = [string]$acc.username
                    password = [string]$acc.password
                }
            }
        }
    } catch {
        return $null
    }
    return $null
}

function Login-Token {
    param(
        [string]$Username,
        [string]$Password
    )
    $body = @{ username = $Username; password = $Password } | ConvertTo-Json
    $resp = Invoke-RestMethod -Uri "$BackendUrl/api/auth/login" -Method Post -ContentType "application/json" -Body $body -TimeoutSec 12
    Assert-ApiCode -Name "login($Username)" -Resp $resp -AllowedCodes @(200)
    if (-not $resp.data -or -not $resp.data.token) {
        throw "login($Username) failed: missing data.token"
    }
    return [string]$resp.data.token
}

function Get-OrgMode {
    $resp = Invoke-RestMethod -Uri "$BackendUrl/api/system/school-config" -Method Get -TimeoutSec 12
    Assert-ApiCode -Name "school-config" -Resp $resp -AllowedCodes @(200)
    if ($resp.data -and $resp.data.orgMode) { return [string]$resp.data.orgMode }
    if ($resp.data -and $resp.data.org_mode) { return [string]$resp.data.org_mode }
    return ""
}

function Is-K12Mode {
    param([string]$OrgMode)
    if (-not $OrgMode) { return $false }
    return ($OrgMode.ToUpperInvariant() -eq "K12" -or $OrgMode.ToUpperInvariant() -eq "HIGH_SCHOOL")
}

function Validate-TreeTypes {
    param(
        [object[]]$Nodes,
        [string[]]$AllowedTypes
    )
    if ($null -eq $Nodes) { throw "department tree data is null" }
    foreach ($n in $Nodes) {
        if (-not $n.type) { throw "tree node missing type: label=$($n.label)" }
        if (-not ($AllowedTypes -contains [string]$n.type)) {
            throw "unexpected node type=$($n.type), allowed=[$($AllowedTypes -join ',')] label=$($n.label)"
        }
        if ($n.children -and $n.children.Count -gt 0) {
            Validate-TreeTypes -Nodes $n.children -AllowedTypes $AllowedTypes
        }
    }
}

Write-Host "=== LZ Sports Department Tree Smoke ==="
Write-Host "Backend : $BackendUrl"
Write-Host "Accounts: $AccountsFile"
Write-Host "DryRun  : $DryRun"

if (-not $UserUsername -or -not $UserPassword) {
    $resolvedUser = Resolve-UserLoginFromAccountsFile
    if ($resolvedUser) {
        $UserUsername = $resolvedUser.username
        $UserPassword = $resolvedUser.password
        Write-Host "[DEPT-TREE] Resolved USER from accounts file: $UserUsername"
    }
}
if (-not $UserUsername -or -not $UserPassword) {
    throw "Unable to resolve USER credentials. Provide -UserUsername/-UserPassword or a valid -AccountsFile."
}

if ($DryRun) {
    Write-Host "=== Department Tree Smoke Completed (DryRun) ==="
    exit 0
}

$orgMode = Invoke-Step -Name "Fetch school-config orgMode" -Action { Get-OrgMode }
Write-Host "[DEPT-TREE] orgMode=$orgMode"

Invoke-Step -Name "Anonymous department/tree should be 401" -Action {
    try {
        Invoke-RestMethod -Uri "$BackendUrl/api/department/tree" -Method Get -TimeoutSec 12 | Out-Null
        throw "expected 401 but request succeeded"
    } catch {
        $raw = $_.ErrorDetails.Message
        if (-not $raw -or $raw -notmatch '"code"\s*:\s*401') {
            throw "expected 401 response for anonymous department/tree"
        }
    }
} | Out-Null

$userToken = Invoke-Step -Name "Login USER ($UserUsername)" -Action { Login-Token -Username $UserUsername -Password $UserPassword }
$headers = @{ Authorization = "Bearer $userToken" }

$tree = Invoke-Step -Name "Fetch department/tree as USER" -Action {
    Invoke-RestMethod -Uri "$BackendUrl/api/department/tree" -Method Get -Headers $headers -TimeoutSec 12
}
Assert-ApiCode -Name "department-tree(user)" -Resp $tree -AllowedCodes @(200)

if (-not $tree.data -or $tree.data.Count -eq 0) {
    throw "department tree is empty"
}

$allowed = @("DEPARTMENT","CLASS","GRADE","COLLEGE","MAJOR")
$expected = if (Is-K12Mode -OrgMode $orgMode) { @("DEPARTMENT","GRADE","CLASS") } else { @("DEPARTMENT","COLLEGE","MAJOR","CLASS") }
Invoke-Step -Name "Validate node types for orgMode" -Action {
    Validate-TreeTypes -Nodes $tree.data -AllowedTypes $expected
} | Out-Null

Write-Host "=== Department Tree Smoke Completed Successfully ==="

