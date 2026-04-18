@echo off
setlocal enabledelayedexpansion

cd /d "%~dp0\.."

for /f %%b in ('git branch --show-current 2^>nul') do set CUR_BRANCH=%%b
if not "%CUR_BRANCH%"=="git-ai/automation-route" (
  echo [ERROR] This test entrypoint is restricted to branch git-ai/automation-route.
  echo         Current branch: %CUR_BRANCH%
  echo         Please switch (or use the automation worktree) before iterative dev/test.
  exit /b 1
)

if /i "%~1"=="smoke" (
  set BACKEND_URL=%~2
  set ACCESS_TOKEN=%~3
  if "!BACKEND_URL!"=="" set BACKEND_URL=http://localhost:8080
  if "!ACCESS_TOKEN!"=="" (
    powershell -ExecutionPolicy Bypass -File "scripts/smoke-linkup.ps1" -BackendUrl "!BACKEND_URL!"
  ) else (
    powershell -ExecutionPolicy Bypass -File "scripts/smoke-linkup.ps1" -BackendUrl "!BACKEND_URL!" -AccessToken "!ACCESS_TOKEN!"
  )
  exit /b !ERRORLEVEL!
)

if /i "%~1"=="notify-smoke" (
  set BACKEND_URL=%~2
  set ACCESS_TOKEN=%~3
  if "!BACKEND_URL!"=="" set BACKEND_URL=http://localhost:8080
  if "!ACCESS_TOKEN!"=="" (
    powershell -ExecutionPolicy Bypass -File "scripts/smoke-notification.ps1" -BackendUrl "!BACKEND_URL!"
  ) else (
    powershell -ExecutionPolicy Bypass -File "scripts/smoke-notification.ps1" -BackendUrl "!BACKEND_URL!" -AccessToken "!ACCESS_TOKEN!"
  )
  exit /b !ERRORLEVEL!
)

if /i "%~1"=="full-smoke" (
  set BACKEND_URL=%~2
  set ACCESS_TOKEN=%~3
  if "!BACKEND_URL!"=="" set BACKEND_URL=http://localhost:8080
  if "!ACCESS_TOKEN!"=="" (
    powershell -ExecutionPolicy Bypass -File "scripts/smoke-full.ps1" -BackendUrl "!BACKEND_URL!"
  ) else (
    powershell -ExecutionPolicy Bypass -File "scripts/smoke-full.ps1" -BackendUrl "!BACKEND_URL!" -AccessToken "!ACCESS_TOKEN!"
  )
  exit /b !ERRORLEVEL!
)

if /i "%~1"=="oneclick" (
  set BACKEND_URL=%~2
  set FRONTEND_URL=%~3
  set EVENT_ID=%~4
  if "!BACKEND_URL!"=="" set BACKEND_URL=http://localhost:8080
  if "!FRONTEND_URL!"=="" set FRONTEND_URL=http://localhost:5173
  if "!EVENT_ID!"=="" set EVENT_ID=1
  powershell -ExecutionPolicy Bypass -File "scripts/smoke-oneclick.ps1" -BackendUrl "!BACKEND_URL!" -FrontendUrl "!FRONTEND_URL!" -EventId !EVENT_ID!
  exit /b !ERRORLEVEL!
)

if /i "%~1"=="dev-start" (
  powershell -ExecutionPolicy Bypass -File "scripts/dev-start.ps1"
  exit /b !ERRORLEVEL!
)

if /i "%~1"=="dev-stop" (
  powershell -ExecutionPolicy Bypass -File "scripts/dev-stop.ps1"
  exit /b !ERRORLEVEL!
)

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
