@echo off
title BossWise - RuneLite Bossing Assistant
cd /d "%~dp0"

echo =========================================================================
echo   Starting BossWise (RuneLite Developer Mode)
echo =========================================================================
echo.

:: Auto-detect Java installation if JAVA_HOME is not explicitly set
if not defined JAVA_HOME (
    if exist "C:\Program Files\Eclipse Adoptium\jdk-17.0.11.9-hotspot" (
        set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.11.9-hotspot"
    ) else if exist "%LOCALAPPDATA%\RuneLite\jre" (
        set "JAVA_HOME=%LOCALAPPDATA%\RuneLite\jre"
    ) else (
        for /d %%d in ("C:\Program Files\Eclipse Adoptium\jdk-*") do set "JAVA_HOME=%%d"
    )
)
if not defined JAVA_HOME (
    for /d %%d in ("C:\Program Files\Java\jdk-*") do set "JAVA_HOME=%%d"
)

if defined JAVA_HOME (
    echo [INFO] Found Java runtime: %JAVA_HOME%
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

echo [INFO] Checking dependencies and compiling BossWise...
echo [INFO] RuneLite client will launch automatically once ready.
echo.

call gradlew.bat run --no-daemon

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo =========================================================================
    echo  [NOTICE] If the launch did not complete:
    echo  1. Make sure you have Java 11 or 17 installed:
    echo     https://adoptium.net/temurin/releases/?version=17
    echo  2. Close any already open RuneLite client windows and try again.
    echo =========================================================================
    echo.
    pause
)
