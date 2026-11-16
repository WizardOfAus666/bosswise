# BossWise - Standalone Sidebar Preview for PowerShell
$ErrorActionPreference = "Stop"
$projDir = $PSScriptRoot

Write-Host "=========================================================================" -ForegroundColor Cyan
Write-Host " Launching BossWise Standalone Sidebar Preview..." -ForegroundColor Cyan
Write-Host "=========================================================================" -ForegroundColor Cyan

$jdkHome = "C:\Program Files\Eclipse Adoptium\jdk-17.0.11.9-hotspot"
$java = if (Test-Path "$jdkHome\bin\java.exe") { "$jdkHome\bin\java.exe" } elseif ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\java.exe")) { "$env:JAVA_HOME\bin\java.exe" } else { "java" }

if (-not (Test-Path "$projDir\target\jvm-preview-args.txt")) {
    Write-Host "[INFO] Generating runtime configuration..." -ForegroundColor Yellow
    & powershell -NoProfile -ExecutionPolicy Bypass -File "$projDir\build.ps1" verify
}

Write-Host "[INFO] Opening BossWise Preview Panel..." -ForegroundColor Green
& $java "@$projDir\target\jvm-preview-args.txt"
