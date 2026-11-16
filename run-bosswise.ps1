# BossWise - RuneLite Client Launcher
$ErrorActionPreference = "Stop"
$projDir = $PSScriptRoot

Write-Host "=========================================================================" -ForegroundColor Cyan
Write-Host " Launching BossWise in RuneLite Developer Mode..." -ForegroundColor Cyan
Write-Host " Client Version: 1.13.6 (Official OSRS Live Server Revision)" -ForegroundColor DarkGray
Write-Host "=========================================================================" -ForegroundColor Cyan

# 1. Close any running RuneLite instances so the new BossWise-injected instance isn't blocked
$running = Get-Process -Name "RuneLite*", "java*" -ErrorAction SilentlyContinue | Where-Object {
    $_.MainWindowTitle -match "RuneLite|Old School" -or $_.ProcessName -match "RuneLite"
}
if ($running) {
    Write-Host "[WARNING] Existing RuneLite process detected (PID: $($running.Id -join ', '))." -ForegroundColor Yellow
    Write-Host "[INFO] Closing existing RuneLite window so BossWise can attach cleanly..." -ForegroundColor Yellow
    $running | Stop-Process -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 1
}

# 2. Remove any duplicate sideloaded BossWise jar (prevents RuneLite duplicate plugin conflict)
try {
    $sideloadDir = "$env:USERPROFILE\.runelite\sideloaded-plugins"
    if (Test-Path $sideloadDir -ErrorAction SilentlyContinue) {
        Get-ChildItem -Path $sideloadDir -Filter "bosswise*.jar" -ErrorAction SilentlyContinue | ForEach-Object {
            Write-Host "[INFO] Removing duplicate sideloaded plugin: $($_.Name)" -ForegroundColor Yellow
            Remove-Item $_.FullName -Force -ErrorAction SilentlyContinue
        }
    }
} catch {}

# 3. Ensure BossWise is enabled by default in RuneLite profile configs
try {
    $profilesDir = "$env:USERPROFILE\.runelite\profiles2"
    if (Test-Path $profilesDir -ErrorAction SilentlyContinue) {
        Get-ChildItem -Path "$profilesDir\*.properties" -ErrorAction SilentlyContinue | ForEach-Object {
            try {
                $content = Get-Content $_.FullName
                $hasKey = $false
                $newContent = $content | ForEach-Object {
                    if ($_ -match "^runelite\.bosswise=") {
                        $hasKey = $true
                        "runelite.bosswise=true"
                    } else {
                        $_
                    }
                }
                if (-not $hasKey) {
                    $newContent += "runelite.bosswise=true"
                }
                Set-Content -Path $_.FullName -Value $newContent -Encoding utf8
            } catch {}
        }
    }
} catch {}

# 4. Locate Java 17
$jdkHome = "C:\Program Files\Eclipse Adoptium\jdk-17.0.11.9-hotspot"
$java = if (Test-Path "$jdkHome\bin\java.exe") { "$jdkHome\bin\java.exe" } elseif ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\java.exe")) { "$env:JAVA_HOME\bin\java.exe" } else { "java" }

Write-Host "[INFO] Verifying and compiling BossWise..." -ForegroundColor Yellow
& powershell -NoProfile -ExecutionPolicy Bypass -File "$projDir\build.ps1" verify
if ($LASTEXITCODE -ne 0) {
    Write-Error "BossWise build failed. Aborting client launch."
    exit $LASTEXITCODE
}

Write-Host "[INFO] Booting RuneLite client with BossWise attached..." -ForegroundColor Green
& $java "@$projDir\target\jvm-args.txt"
