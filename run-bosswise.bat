@echo off
setlocal
cd /d "%~dp0"

echo =========================================================================
echo  Launching BossWise in RuneLite Developer Mode...
echo  Client Version: 1.13.6 (Official OSRS Live Server Revision)
echo =========================================================================

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run-bosswise.ps1"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Launch failed with code %ERRORLEVEL%.
    pause
)
endlocal
