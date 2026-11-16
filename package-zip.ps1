$ProgressPreference = 'SilentlyContinue'
$sourceDir = "C:\Users\Gaige\.gemini\antigravity\scratch\bosswise"
$zipPath = "C:\Users\Gaige\.gemini\antigravity\scratch\BossWise-Share-Package.zip"

if (Test-Path $zipPath) {
    Remove-Item $zipPath -Force
}

Add-Type -AssemblyName System.IO.Compression.FileSystem
[System.IO.Compression.ZipFile]::CreateFromDirectory($sourceDir, $zipPath)

$item = Get-Item $zipPath
Write-Host "Created ZIP: $($item.FullName) ($([Math]::Round($item.Length / 1MB, 2)) MB)"
