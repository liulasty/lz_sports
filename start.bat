@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul
color 0F
mode con: cols=120 lines=30

:: 启用虚拟终端处理（Windows 10+）
reg query "HKCU\Console" /v VirtualTerminalLevel >nul 2>&1
if errorlevel 1 (
    reg add "HKCU\Console" /v VirtualTerminalLevel /t REG_DWORD /d 1 /f >nul 2>&1
)

:: 生成颜色变量
for /f %%A in ('echo prompt $E ^| cmd') do set "ESC=%%A"

:: ==============================================
:: 第一步：自动生成时间戳标签
:: ==============================================
for /f "tokens=2 delims==" %%a in ('wmic path win32_localtime get year /value') do set "YYYY=%%a"
for /f "tokens=2 delims==" %%a in ('wmic path win32_localtime get month /value') do set "MM=%%a"
for /f "tokens=2 delims==" %%a in ('wmic path win32_localtime get day /value') do set "DD=%%a"
for /f "tokens=2 delims==" %%a in ('wmic path win32_localtime get hour /value') do set "HH=%%a"
for /f "tokens=2 delims==" %%a in ('wmic path win32_localtime get minute /value') do set "MN=%%a"
for /f "tokens=2 delims==" %%a in ('wmic path win32_localtime get second /value') do set "SS=%%a"

set MM=0%MM%
set MM=%MM:~-2%
set DD=0%DD%
set DD=%DD:~-2%
set HH=0%HH%
set HH=%HH:~-2%
set MN=0%MN%
set MN=%MN:~-2%
set SS=0%SS%
set SS=%SS:~-2%

set BUILD_TAG=%YYYY%%MM%%DD%%HH%%MN%%SS%
echo.
echo  ==============================================
echo  📅 自动生成构建版本标签：%BUILD_TAG%
echo  ==============================================
echo.

:: ==============================================
:: 第二步：停止旧容器
:: ==============================================
echo  [1/5] 正在停止现有服务...
docker-compose down
echo  ✅ 服务已停止
echo.

:: ==============================================
:: 第三步：清理无用容器
:: ==============================================
echo  [2/5] 清理已停止容器...
docker container prune -f
echo  ✅ 容器清理完成
echo.

:: ==============================================
:: 第四步：清理虚悬镜像
:: ==============================================
echo  [3/5] 清理虚悬镜像...
docker image prune -f
echo  ✅ 虚悬镜像清理完成
echo.

:: ==============================================
:: 第五步：保留最新3个镜像，删除旧镜像
:: ==============================================
echo  [4/5] 清理历史镜像，仅保留最新3个...
for /f "skip=3 tokens=*" %%i in ('docker images lz-sports-* --format "{{.ID}}"') do (
    docker rmi -f %%i >nul 2>&1
)
echo  ✅ 历史镜像清理完成
echo.

:: ==============================================
:: 第六步：构建并启动
:: ==============================================
echo  [5/5] 构建最新镜像并启动服务...
set "BUILD_TAG=%BUILD_TAG%"
docker-compose up -d --build
echo.
echo  ==============================================
echo  🚀 服务启动完成！
echo  ==============================================
echo.

:: ==============================================
:: 【新增】彩色状态检查
:: ==============================================
echo  🔖 当前发布版本：
echo    后端：lz-sports-backend:%BUILD_TAG%
echo    前端：lz-sports-frontend:%BUILD_TAG%
echo.

:: 检查状态
for /f "delims=" %%a in ('docker inspect --format "{{.State.Status}}" lz-sports-backend 2^>nul') do set "BackendStatus=%%a"
for /f "delims=" %%a in ('docker inspect --format "{{.State.Status}}" lz-sports-frontend 2^>nul') do set "FrontendStatus=%%a"

if not defined BackendStatus set "BackendStatus=stopped"
if not defined FrontendStatus set "FrontendStatus=stopped"

:: 定义颜色代码
set "GREEN=%ESC%[92m"
set "RED=%ESC%[91m"
set "CYAN=%ESC%[96m"
set "RESET=%ESC%[0m"

:: 输出状态
echo  🚥 容器运行状态：
if "%BackendStatus%"=="running" (
    echo    后端：!GREEN!RUNNING!RESET!
) else (
    echo    后端：!RED!!BackendStatus!!RESET!
)
if "%FrontendStatus%"=="running" (
    echo    前端：!GREEN!RUNNING!RESET!
) else (
    echo    前端：!RED!!FrontendStatus!!RESET!
)
echo.

echo  🌐 访问地址：
echo    前端：!CYAN!http://localhost:5173!RESET!
echo    后端：!CYAN!http://localhost:8080!RESET!
echo.

echo  ==============================================
echo  操作全部完成！
echo  ==============================================
echo.
pause