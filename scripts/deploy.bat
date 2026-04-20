@echo off
setlocal enabledelayedexpansion

cd /d "%~dp0\.."

set ENV=dev
set VERSION=latest
set USE_LOCAL_DB=false

:parse_args
if "%~1"=="" goto end_args
if /I "%~1"=="prod" (
    set ENV=prod
    shift
    goto parse_args
)
if /I "%~1"=="dev" (
    set ENV=dev
    shift
    goto parse_args
)
if /I "%~1"=="--local-db" (
    set USE_LOCAL_DB=true
    shift
    goto parse_args
)
:: 假设如果不是以上固定参数，则是版本号
set VERSION=%~1
shift
goto parse_args

:end_args

echo ========================================================
echo Deploying environment: %ENV%
echo Version: %VERSION%
if "%USE_LOCAL_DB%"=="true" (
    echo Using LOCAL Database and Redis (host.docker.internal^)
) else (
    echo Using CONTAINERIZED Database and Redis
)
echo ========================================================

set IMAGE_VERSION=%VERSION%

:: 组装 COMPOSE 命令文件
set COMPOSE_FILES=-f docker-compose.yml
if "%ENV%"=="prod" (
    set COMPOSE_FILES=%COMPOSE_FILES% -f docker-compose.prod.yml
    set ENV_FILE=config/.env.prod
) else (
    set COMPOSE_FILES=%COMPOSE_FILES% -f docker-compose.dev.yml
    set ENV_FILE=config/.env.dev
)

:: 如果启用 --local-db，追加覆盖文件
if "%USE_LOCAL_DB%"=="true" (
    set COMPOSE_FILES=%COMPOSE_FILES% -f docker-compose.local-db.yml
)

:: 执行 Docker Compose
echo Running: docker compose --env-file %ENV_FILE% %COMPOSE_FILES% up -d
docker compose --env-file %ENV_FILE% %COMPOSE_FILES% up -d

echo Deployment complete.
