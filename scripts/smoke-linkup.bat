@echo off
setlocal

powershell -ExecutionPolicy Bypass -File "%~dp0smoke-linkup.ps1" %*
set EXIT_CODE=%ERRORLEVEL%

if not %EXIT_CODE%==0 (
  echo Smoke failed with exit code %EXIT_CODE%
  exit /b %EXIT_CODE%
)

echo Smoke passed.
exit /b 0
