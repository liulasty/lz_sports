@echo off
setlocal

cd /d "%~dp0\.."

set VERSION=%~1
if "%VERSION%"=="" set VERSION=latest
set IMAGE_VERSION=%VERSION%

echo ========================================================
echo   [Build] Building project version: %VERSION%
echo ========================================================

echo 1. Compiling backend...
cd lz_sports_backend
call mvn clean package -DskipTests
if %ERRORLEVEL% neq 0 exit /b %ERRORLEVEL%
cd ..

echo 2. Building Docker images...
docker compose build
if %ERRORLEVEL% neq 0 exit /b %ERRORLEVEL%

echo Build complete for version %VERSION%.
