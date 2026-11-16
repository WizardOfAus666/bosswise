$path = 'C:\Users\Gaige\.gemini\antigravity\brain\b4976cdb-d323-4b3a-a440-01599d75173a\.system_generated\steps\4436\output.txt'
if (Test-Path $path) {
    $lines = Get-Content $path
    $clipped = @()
    foreach ($line in $lines) {
        if ($line.StartsWith("[CLIPPED]")) {
            $clean = $line -replace ' @\d+px', ''
            $clipped += $clean
        }
    }
    $grouped = $clipped | Group-Object | Sort-Object -Descending Count
    Write-Host "Found $($clipped.Count) clipping instances, $($grouped.Count) unique:"
    foreach ($g in $grouped) {
        Write-Host "$($g.Count)x : $($g.Name)"
    }
} else {
    Write-Host "File not found: $path"
}
