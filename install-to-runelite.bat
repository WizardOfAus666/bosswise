@echo off
setlocal
cd /d "%~dp0"

echo =========================================================================
echo  Installing BossWise into your installed RuneLite Client...
echo =========================================================================

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0install-to-runelite.ps1"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Installation failed with code %ERRORLEVEL%.
    pause
)
endlocal
