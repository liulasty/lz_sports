@echo off
setlocal enabledelayedexpansion

cd /d "%~dp0\.."

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

if /i "%~1"=="suite" (
  set BACKEND_URL=%~2
  set FRONTEND_URL=%~3
  set EVENT_ID=%~4
  set ACCOUNTS_FILE=%~5
  if "!BACKEND_URL!"=="" set BACKEND_URL=http://localhost:8080
  if "!FRONTEND_URL!"=="" set FRONTEND_URL=http://localhost:5173
  if "!EVENT_ID!"=="" set EVENT_ID=1
  if "!ACCOUNTS_FILE!"=="" set ACCOUNTS_FILE=git-ai/automation-route/runs/business-accounts-latest.json
  powershell -ExecutionPolicy Bypass -File "scripts/smoke-suite.ps1" -BackendUrl "!BACKEND_URL!" -FrontendUrl "!FRONTEND_URL!" -EventId !EVENT_ID! -AccountsFile "!ACCOUNTS_FILE!"
  exit /b !ERRORLEVEL!
)

if /i "%~1"=="user-status-smoke" (
  set BACKEND_URL=%~2
  set ACCOUNTS_FILE=%~3
  if "!BACKEND_URL!"=="" set BACKEND_URL=http://localhost:8080
  if "!ACCOUNTS_FILE!"=="" set ACCOUNTS_FILE=git-ai/automation-route/runs/business-accounts-latest.json
  powershell -ExecutionPolicy Bypass -File "scripts/smoke-user-status.ps1" -BackendUrl "!BACKEND_URL!" -AccountsFile "!ACCOUNTS_FILE!"
  exit /b !ERRORLEVEL!
)

if /i "%~1"=="event-admin-smoke" (
  set BACKEND_URL=%~2
  set ACCOUNTS_FILE=%~3
  if "!BACKEND_URL!"=="" set BACKEND_URL=http://localhost:8080
  if "!ACCOUNTS_FILE!"=="" set ACCOUNTS_FILE=git-ai/automation-route/runs/business-accounts-latest.json
  powershell -ExecutionPolicy Bypass -File "scripts/smoke-event-admin.ps1" -BackendUrl "!BACKEND_URL!" -AccountsFile "!ACCOUNTS_FILE!"
  exit /b !ERRORLEVEL!
)

if /i "%~1"=="forgot-once" (
  set BACKEND_URL=%~2
  set USERNAME=%~3
  set EMAIL=%~4
  if "!BACKEND_URL!"=="" set BACKEND_URL=http://localhost:8080
  if "!USERNAME!"=="" set USERNAME=autou_164656_10
  if "!EMAIL!"=="" set EMAIL=auto16465610@qq.com
  powershell -ExecutionPolicy Bypass -File "scripts/smoke-forgot-once.ps1" -BackendUrl "!BACKEND_URL!" -Username "!USERNAME!" -Email "!EMAIL!"
  exit /b !ERRORLEVEL!
)

if /i "%~1"=="dev-start" (
  powershell -ExecutionPolicy Bypass -File "scripts/dev-start.ps1"
  exit /b !ERRORLEVEL!
)

if /i "%~1"=="aicoding-precheck" (
  powershell -ExecutionPolicy Bypass -File "scripts/aicoding-precheck.ps1"
  exit /b !ERRORLEVEL!
)

if /i "%~1"=="dev-stop" (
  powershell -ExecutionPolicy Bypass -File "scripts/dev-stop.ps1"
  exit /b !ERRORLEVEL!
)

if /i "%~1"=="publish" (
  powershell -ExecutionPolicy Bypass -File "scripts/publish.ps1"
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
