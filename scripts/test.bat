@echo off
setlocal enabledelayedexpansion

cd /d "%~dp0\.."

set ENV=%~1
if "%ENV%"=="" set ENV=dev

if "%ENV%"=="prod" (
  set PORT=80
) else (
  set PORT=5173
)

echo Waiting for services to start...
timeout /t 5 > nul

echo Testing Frontend on port %PORT%...
curl -sSf -m 5 http://localhost:%PORT% > nul
if !ERRORLEVEL! neq 0 (
  echo [ERROR] Frontend is not reachable!
  exit /b 1
)
echo [OK] Frontend is up.

echo Testing Backend through proxy...
curl -sSf -m 5 http://localhost:%PORT%/api/system/init-status > nul
if !ERRORLEVEL! neq 0 (
  echo [ERROR] Backend is not reachable or healthy!
  exit /b 1
)
echo [OK] Backend is healthy.

echo All tests passed.
