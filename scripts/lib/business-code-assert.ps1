function Assert-BusinessCode {
    param(
        [Parameter(Mandatory = $true)][string]$Name,
        [Parameter(Mandatory = $true)][object]$Resp,
        [Parameter(Mandatory = $true)][int[]]$AllowedCodes
    )
    if ($null -eq $Resp -or -not ($Resp.PSObject.Properties.Name -contains "code")) {
        throw "$Name failed: invalid business response"
    }
    $actual = [int]$Resp.code
    if (-not ($AllowedCodes -contains $actual)) {
        throw "$Name failed: expected code in [$($AllowedCodes -join ',')], got code=$actual, msg=$($Resp.msg)"
    }
}

function Assert-ExpectedBusinessCode {
    param(
        [Parameter(Mandatory = $true)][string]$Name,
        [Parameter(Mandatory = $true)][scriptblock]$Action,
        [Parameter(Mandatory = $true)][int]$ExpectedCode
    )
    try {
        $resp = & $Action
        if ($null -ne $resp -and ($resp.PSObject.Properties.Name -contains "code")) {
            if ([int]$resp.code -eq $ExpectedCode) {
                return $resp
            }
            throw "$Name failed: expected code=$ExpectedCode, got code=$($resp.code), msg=$($resp.msg)"
        }
    } catch {
        if ($_.ErrorDetails -and $_.ErrorDetails.Message) {
            $parsed = $_.ErrorDetails.Message | ConvertFrom-Json
            if ($parsed -and ($parsed.PSObject.Properties.Name -contains "code") -and [int]$parsed.code -eq $ExpectedCode) {
                return $parsed
            }
            throw "$Name failed: expected code=$ExpectedCode, got code=$($parsed.code), msg=$($parsed.msg)"
        }
        $response = $_.Exception.Response
        if ($response) {
            $reader = New-Object System.IO.StreamReader($response.GetResponseStream())
            $body = $reader.ReadToEnd()
            $reader.Close()
            $parsed = $body | ConvertFrom-Json
            if ($parsed -and ($parsed.PSObject.Properties.Name -contains "code") -and [int]$parsed.code -eq $ExpectedCode) {
                return $parsed
            }
            throw "$Name failed: expected code=$ExpectedCode, got code=$($parsed.code), msg=$($parsed.msg)"
        }
        throw
    }
    throw "$Name failed: expected code=$ExpectedCode but request succeeded unexpectedly"
}
