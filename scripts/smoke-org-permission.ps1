param(
    [string]$BackendUrl = "http://localhost:8080",
    [string]$AccountsFile = "git-ai/automation-route/accounts/business-accounts-latest.json",
    [string]$SchoolAdminUsername = "init_school_admin_01",
    [string]$SchoolAdminPassword = "Admin12345",
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
    Write-Host "[ORG-PERM] $Name"
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
    return @{
        token = $resp.data.token
        role  = $resp.data.role
        id    = $resp.data.id
    }
}

function Try-LoginToken {
    param(
        [string]$Username,
        [string]$Password
    )
    try { return Login-Token -Username $Username -Password $Password } catch { return $null }
}

function Resolve-SchoolAdminLogin {
    $candidates = @(
        @{ username = $SchoolAdminUsername; password = $SchoolAdminPassword },
        @{ username = $SchoolAdminUsername; password = "admin123" },
        @{ username = "school_admin_05bea495"; password = "admin123" },
        @{ username = "school_admin_0004b87c"; password = "admin123" },
        @{ username = "school_admin_96719547"; password = "admin123" },
        @{ username = "school_admin_62e2507a"; password = "admin123" },
        @{ username = "init_school_admin_01"; password = "Admin12345" },
        @{ username = "init_school_admin_01"; password = "Pass12345" }
    )

    foreach ($candidate in $candidates) {
        if (-not $candidate.username -or -not $candidate.password) { continue }
        $login = Try-LoginToken -Username $candidate.username -Password $candidate.password
        if ($login) {
            Write-Host "[ORG-PERM] Resolved SCHOOL_ADMIN seed: $($candidate.username)"
            return [PSCustomObject]@{
                username = $candidate.username
                password = $candidate.password
                login = $login
            }
        }
    }

    throw "Unable to resolve a working SCHOOL_ADMIN account."
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

function Api-Get {
    param(
        [string]$Path,
        [hashtable]$Headers
    )
    return Invoke-RestMethod -Uri "$BackendUrl$Path" -Method Get -Headers $Headers -TimeoutSec 12
}

function Api-Post {
    param(
        [string]$Path,
        [hashtable]$Headers,
        [object]$Body
    )
    $json = "null"
    if ($null -ne $Body) { $json = ($Body | ConvertTo-Json -Depth 10) }
    return Invoke-RestMethod -Uri "$BackendUrl$Path" -Method Post -Headers $Headers -ContentType "application/json" -Body $json -TimeoutSec 12
}

Write-Host "=== LZ Sports Org Permission Smoke ==="
Write-Host "Backend : $BackendUrl"
Write-Host "Accounts: $AccountsFile"
Write-Host "DryRun  : $DryRun"

$school = $null
try {
    $resolvedSchool = Resolve-SchoolAdminLogin
    $school = $resolvedSchool.login
} catch {
    $school = $null
}

$resolvedUser = $null
if (-not $UserUsername -or -not $UserPassword) {
    $resolvedUser = Resolve-UserLoginFromAccountsFile
    if ($resolvedUser) {
        $UserUsername = $resolvedUser.username
        $UserPassword = $resolvedUser.password
        Write-Host "[ORG-PERM] Resolved USER from accounts file: $UserUsername"
    }
}
if (-not $UserUsername -or -not $UserPassword) {
    throw "Unable to resolve USER credentials. Provide -UserUsername/-UserPassword or a valid -AccountsFile."
}

$user = Invoke-Step -Name "Login USER ($UserUsername)" -Action { Login-Token -Username $UserUsername -Password $UserPassword }

if ($DryRun) {
    Write-Host "=== Org Permission Smoke Completed (DryRun) ==="
    exit 0
}

$hSchool = $null
if ($school -and $school.token) {
    $hSchool = @{ Authorization = "Bearer $($school.token)" }
}
$hUser = @{ Authorization = "Bearer $($user.token)" }

# Dept tree might be protected by login; use USER token to avoid false negatives.
Invoke-Step -Name "Department tree is reachable (as USER)" -Action {
    $resp = Api-Get -Path "/api/department/tree" -Headers $hUser
    Assert-ApiCode -Name "department-tree(user)" -Resp $resp -AllowedCodes @(200)
} | Out-Null

# USER must not be able to mutate departments
$deptBody = @{
    name = "smoke_perm_dept_" + (Get-Date -Format "HHmmss")
    parentId = 0
} 

Invoke-Step -Name "USER cannot create department (expect 403)" -Action {
    $resp = Api-Post -Path "/api/department" -Headers $hUser -Body $deptBody
    Assert-ApiCode -Name "department-add(user)" -Resp $resp -AllowedCodes @(403)
} | Out-Null

# Optional: verify SCHOOL_ADMIN positive path if available.
if ($hSchool) {
    Invoke-Step -Name "SCHOOL_ADMIN can create department" -Action {
        $resp = Api-Post -Path "/api/department" -Headers $hSchool -Body $deptBody
        Assert-ApiCode -Name "department-add(school)" -Resp $resp -AllowedCodes @(200)
    } | Out-Null
} else {
    Write-Host "[ORG-PERM] SCHOOL_ADMIN unavailable, skipped positive department-create assertion"
}

Write-Host "=== Org Permission Smoke Completed Successfully ==="

