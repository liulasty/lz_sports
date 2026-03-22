@echo off
setlocal
set "SCRIPT_DIR=%~dp0"
where pwsh >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    pwsh -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT_DIR%publish.ps1"
    goto end
)
powershell -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT_DIR%publish.ps1"

:end
pause
exit /b %ERRORLEVEL%