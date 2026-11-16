# BossWise - RuneLite Plugin Launcher & Desktop Shortcut Installer
$ErrorActionPreference = "Stop"
$projDir = $PSScriptRoot

Write-Host "=========================================================================" -ForegroundColor Cyan
Write-Host " Setting up BossWise for RuneLite..." -ForegroundColor Cyan
Write-Host "=========================================================================" -ForegroundColor Cyan

# 1. Build and verify latest BossWise plugin
Write-Host "[INFO] Compiling and verifying latest BossWise build..." -ForegroundColor Yellow
& powershell -NoProfile -ExecutionPolicy Bypass -File "$projDir\build.ps1" verify

# 2. Create Desktop Shortcut for easy 1-click launching
try {
    $desktopDir = [System.Environment]::GetFolderPath([System.Environment+SpecialFolder]::Desktop)
    if (Test-Path $desktopDir) {
        $shortcutPath = "$desktopDir\BossWise (RuneLite).lnk"
        $wsh = New-Object -ComObject WScript.Shell
        $shortcut = $wsh.CreateShortcut($shortcutPath)
        $shortcut.TargetPath = "$projDir\run-bosswise.bat"
        $shortcut.WorkingDirectory = "$projDir"
        $shortcut.IconLocation = "$projDir\icon.png"
        $shortcut.Description = "Launch RuneLite with BossWise Plugin"
        $shortcut.Save()
        Write-Host "[SUCCESS] Created Desktop Shortcut: BossWise (RuneLite)" -ForegroundColor Green
    }
} catch {
    Write-Host "[NOTE] Desktop shortcut creation skipped: $($_.Exception.Message)" -ForegroundColor DarkGray
}

# 3. Launch RuneLite with BossWise attached
Write-Host "[INFO] Launching RuneLite with BossWise attached..." -ForegroundColor Cyan
& powershell -NoProfile -ExecutionPolicy Bypass -File "$projDir\run-bosswise.ps1"
