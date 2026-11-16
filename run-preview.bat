@echo off
setlocal
cd /d "%~dp0"

echo =========================================================================
echo  Launching BossWise UI Preview Window (Offline / Instant Desktop UI)
echo =========================================================================

set "JAVA_BIN=java"
if exist "C:\Program Files\Eclipse Adoptium\jdk-17.0.11.9-hotspot\bin\java.exe" (
    set "JAVA_BIN=C:\Program Files\Eclipse Adoptium\jdk-17.0.11.9-hotspot\bin\java.exe"
) else if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_BIN=%JAVA_HOME%\bin\java.exe"
)

if not exist "%~dp0target\jvm-preview-args.txt" (
    echo [INFO] Generating runtime configuration and compiling BossWise...
    powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build.ps1" verify
)

echo [INFO] Opening BossWise Sidebar Preview...
"%JAVA_BIN%" "@%~dp0target\jvm-preview-args.txt"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Preview launch failed with code %ERRORLEVEL%.
    pause
)
endlocal
