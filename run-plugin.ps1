# BossWise - Run Plugin Launcher (Alias to run-bosswise.ps1)
$script = Join-Path $PSScriptRoot "run-bosswise.ps1"
& powershell -NoProfile -ExecutionPolicy Bypass -File $script
