<#
.SYNOPSIS
    一键自动发布脚本（含配置备份与镜像版本管理）
.DESCRIPTION
    功能：
    1. 生成带有时间戳的 Docker 镜像 tag（例如：20260321160000）。
    2. 自动更新 docker-compose.yml 中的镜像 tag。
    3. 备份当前 docker-compose.yml，仅保留最近 3 个历史版本。
    4. 执行 Docker 镜像构建与容器部署。
    5. 自动记录版本清单（含 Tag 与 Image ID，保留最新 10 条记录）。
#>

# ========== 1. 基础配置 ==========
$TargetFile = "docker-compose.yml"
$BackupPrefix = "docker-compose_"
$VersionFile = "release-version-history.json"
$KeepCount = 3

$Timestamp = Get-Date -Format "yyyyMMddHHmmss"
$BackendImageRepo = "lz-sports-backend"
$FrontendImageRepo = "lz-sports-frontend"
$BackendImageName = "${BackendImageRepo}:${Timestamp}"
$FrontendImageName = "${FrontendImageRepo}:${Timestamp}"

# 颜色输出辅助函数
function Write-ColorLog {
    param([string]$Message, [string]$Color = "White")
    Write-Host $Message -ForegroundColor $Color
}

Write-ColorLog "🚀 开始发布新版本 (Tag: $Timestamp)..." "Cyan"

# ========== 2. 更新 docker-compose.yml 的镜像 Tag ==========
if (-not (Test-Path $TargetFile)) {
    Write-ColorLog "❌ 错误: 当前目录下找不到文件 $TargetFile" "Red"
    exit 1
}

Write-ColorLog "📝 正在更新 $TargetFile 中的镜像标签..." "Yellow"
$ComposeContent = Get-Content $TargetFile -Raw

$BackendImagePattern = "(?m)^(\s*image:\s*${BackendImageRepo}:)[^\s#]+"
$FrontendImagePattern = "(?m)^(\s*image:\s*${FrontendImageRepo}:)[^\s#]+"

if (-not [regex]::IsMatch($ComposeContent, $BackendImagePattern)) {
    Write-ColorLog "❌ 错误: 在 $TargetFile 中未找到 ${BackendImageRepo} 的 image 配置。" "Red"
    exit 1
}

if (-not [regex]::IsMatch($ComposeContent, $FrontendImagePattern)) {
    Write-ColorLog "❌ 错误: 在 $TargetFile 中未找到 ${FrontendImageRepo} 的 image 配置。" "Red"
    exit 1
}

$ComposeContent = [regex]::Replace($ComposeContent, $BackendImagePattern, ('${1}' + $Timestamp))
$ComposeContent = [regex]::Replace($ComposeContent, $FrontendImagePattern, ('${1}' + $Timestamp))

$BackendTagInCompose = [regex]::Match($ComposeContent, "(?m)^\s*image:\s*${BackendImageRepo}:([^\s#]+)").Groups[1].Value
$FrontendTagInCompose = [regex]::Match($ComposeContent, "(?m)^\s*image:\s*${FrontendImageRepo}:([^\s#]+)").Groups[1].Value

if ($BackendTagInCompose -ne $Timestamp -or $FrontendTagInCompose -ne $Timestamp) {
    Write-ColorLog "❌ 错误: 镜像标签写入失败，当前为 backend=$BackendTagInCompose, frontend=$FrontendTagInCompose" "Red"
    exit 1
}

Set-Content -Path $TargetFile -Value $ComposeContent -Encoding UTF8
Write-ColorLog "✅ 镜像标签已成功更新为: backend=$BackendTagInCompose, frontend=$FrontendTagInCompose" "Green"

# ========== 3. 备份 docker-compose.yml ==========
$BackupFileName = "${BackupPrefix}${Timestamp}.yml"
Copy-Item $TargetFile -Destination $BackupFileName -Force
Write-ColorLog "✅ 已备份当前配置为 $BackupFileName" "Green"

# 获取并清理多余的旧备份
$AllBackups = Get-ChildItem -Path . -Filter "${BackupPrefix}*.yml" | 
              Where-Object { $_.Name -match "docker-compose_\d{14}\.yml" } | 
              Sort-Object Name -Descending

if ($AllBackups.Count -gt $KeepCount) {
    $ToDelete = $AllBackups | Select-Object -Skip $KeepCount
    foreach ($File in $ToDelete) {
        Remove-Item $File.FullName -Force
        Write-ColorLog "🗑️ 已清理过期配置备份 $($File.Name)" "DarkGray"
    }
}

# ========== 4. 构建与部署 Docker 容器 ==========
Write-ColorLog "🐳 开始构建 Docker 镜像..." "Yellow"
$UseDockerComposeV1 = $null -ne (Get-Command docker-compose -ErrorAction SilentlyContinue)
if ($UseDockerComposeV1) {
    docker-compose build backend frontend
} else {
    docker compose build backend frontend
}

if ($LASTEXITCODE -ne 0) {
    Write-ColorLog "❌ 错误: Docker 镜像构建失败，发布终止。" "Red"
    exit 1
}

$LatestBackendImageId = (docker images -q "${BackendImageRepo}:latest")
$LatestFrontendImageId = (docker images -q "${FrontendImageRepo}:latest")

if (-not [string]::IsNullOrWhiteSpace($LatestBackendImageId) -and [string]::IsNullOrWhiteSpace((docker images -q $BackendImageName))) {
    docker tag "${BackendImageRepo}:latest" $BackendImageName
}
if (-not [string]::IsNullOrWhiteSpace($LatestFrontendImageId) -and [string]::IsNullOrWhiteSpace((docker images -q $FrontendImageName))) {
    docker tag "${FrontendImageRepo}:latest" $FrontendImageName
}

Write-ColorLog "🐳 开始启动 Docker 容器..." "Yellow"
if ($UseDockerComposeV1) {
    docker-compose up -d --force-recreate
} else {
    docker compose up -d --force-recreate
}

if ($LASTEXITCODE -ne 0) {
    Write-ColorLog "❌ 错误: Docker 容器启动失败，发布终止。" "Red"
    exit 1
}

# ========== 5. 记录版本发布历史 ==========
Write-ColorLog "📊 获取镜像 ID 并记录版本清单..." "Yellow"

# 获取最新构建的镜像 ID
$BackendImageId = (docker images -q $BackendImageName)
$FrontendImageId = (docker images -q $FrontendImageName)

$BackendIdVal = $BackendImageId
if ([string]::IsNullOrWhiteSpace($BackendIdVal)) {
    $BackendIdVal = "Unknown"
}

$FrontendIdVal = $FrontendImageId
if ([string]::IsNullOrWhiteSpace($FrontendIdVal)) {
    $FrontendIdVal = "Unknown"
}

$VersionInfo = [PSCustomObject]@{
    ReleaseTime   = (Get-Date -Format "yyyy-MM-dd HH:mm:ss")
    VersionTag    = $Timestamp
    BackendImage  = $BackendImageName
    BackendId     = $BackendIdVal
    FrontendImage = $FrontendImageName
    FrontendId    = $FrontendIdVal
}

$History = @()
if (Test-Path $VersionFile) {
    try {
        $History = Get-Content $VersionFile -Raw -ErrorAction SilentlyContinue | ConvertFrom-Json
        if ($History -isnot [array]) { $History = @($History) }
    } catch {
        Write-ColorLog "⚠️ 无法读取旧版本历史，将创建全新记录。" "DarkYellow"
    }
}

# 追加新记录，保留最近 10 条
$History += $VersionInfo
$History = $History | Select-Object -Last 10

# 保存为 JSON 格式（原生支持，不依赖第三方 YAML 模块）
$History | ConvertTo-Json -Depth 5 | Set-Content -Path $VersionFile -Encoding UTF8
Write-ColorLog "✅ 版本清单已成功记录至 $VersionFile" "Green"

# ========== 6. 验证服务状态 ==========
Write-ColorLog "===== 检查发布状态 =====" "Cyan"

# 获取容器运行状态
$BackendStatus = (docker inspect --format '{{.State.Status}}' lz-sports-backend 2>&1)
$FrontendStatus = (docker inspect --format '{{.State.Status}}' lz-sports-frontend 2>&1)

$BackendColor = if ($BackendStatus -match "running") { "Green" } else { "Red" }
$FrontendColor = if ($FrontendStatus -match "running") { "Green" } else { "Red" }

Write-ColorLog "🔖 当前发布版本：" "White"
Write-ColorLog "  后端：$BackendImageName (ID: $($VersionInfo.BackendId))" "White"
Write-ColorLog "  前端：$FrontendImageName (ID: $($VersionInfo.FrontendId))" "White"

Write-ColorLog "🚥 容器运行状态：" "White"
Write-ColorLog "  后端：$BackendStatus" $BackendColor
Write-ColorLog "  前端：$FrontendStatus" $FrontendColor

Write-ColorLog "🌐 访问地址：" "White"
Write-ColorLog "  前端：http://localhost" "Cyan"
Write-ColorLog "  后端：http://localhost:8080" "Cyan"

Write-ColorLog "🎉 恭喜！版本配置备份 + 镜像版本管理发布完成！" "Green"
