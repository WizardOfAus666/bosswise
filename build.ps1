param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$Goals
)

$ErrorActionPreference = "Stop"
$projDir = Split-Path -Parent $MyInvocation.MyCommand.Path

if (-not $Goals -or $Goals.Count -eq 0) {
    $Goals = @("verify")
}

$jdkHome = $env:JAVA_HOME
if (-not $jdkHome -or -not (Test-Path "$jdkHome\bin\javac.exe")) {
    if (Test-Path "C:\Program Files\Eclipse Adoptium\jdk-17.0.11.9-hotspot\bin\javac.exe") {
        $jdkHome = "C:\Program Files\Eclipse Adoptium\jdk-17.0.11.9-hotspot"
    } elseif (Test-Path "C:\Program Files\Java\jdk-21.0.12\bin\javac.exe") {
        $jdkHome = "C:\Program Files\Java\jdk-21.0.12"
    }
}

$javac = if ($jdkHome) { "$jdkHome\bin\javac.exe" } else { "javac" }
$java  = if ($jdkHome) { "$jdkHome\bin\java.exe" }  else { "java" }
$jar   = if ($jdkHome) { "$jdkHome\bin\jar.exe" }   else { "jar" }

Write-Host "[INFO] Scanning for projects..."
Write-Host "[INFO] ------------------------------------------------------------------------"
Write-Host "[INFO] Building BossWise 1.0.0 (com.bosswise:bosswise)"
Write-Host "[INFO] ------------------------------------------------------------------------"

$cacheBase = "$env:USERPROFILE\.gradle\caches\modules-2\files-2.1"
$jars = Get-ChildItem -Path $cacheBase -Recurse -File -Filter "*.jar" -ErrorAction SilentlyContinue | Where-Object {
    $_.Name -notmatch "natives-" -and 
    $_.Name -notmatch "1.13.2-SNAPSHOT" -and 
    $_.Name -notmatch "1.13.1" -and 
    $_.Name -notmatch "1.2.25"
} | Select-Object -ExpandProperty FullName
$cp = ($jars -join ";")

if ($Goals -contains "clean") {
    Write-Host "[INFO] --- clean: deleting target directory ---"
    if (Test-Path "$projDir\target") {
        Remove-Item -Recurse -Force "$projDir\target"
    }
}

New-Item -ItemType Directory -Force -Path "$projDir\target\classes", "$projDir\target\test-classes" | Out-Null
if (Test-Path "$projDir\src\main\resources") {
    Copy-Item -Path "$projDir\src\main\resources\*" -Destination "$projDir\target\classes" -Recurse -Force
}

Write-Host "[INFO] --- compiling main Java sources (Java 11 target) ---"
$mainSources = Get-ChildItem -Path "$projDir\src\main\java" -Recurse -Filter "*.java" | Select-Object -ExpandProperty FullName
if ($mainSources) {
    & $javac --release 11 -encoding UTF-8 -cp $cp -d "$projDir\target\classes" $mainSources
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Compilation failed."
        exit $LASTEXITCODE
    }
    Write-Host "[INFO] Compiled $($mainSources.Count) source files to target\classes"
}

if ($Goals -contains "test-compile" -or $Goals -contains "test" -or $Goals -contains "package" -or $Goals -contains "verify" -or $Goals -contains "install") {
    Write-Host "[INFO] --- compiling test Java sources ---"
    $testSources = Get-ChildItem -Path "$projDir\src\test\java" -Recurse -Filter "*.java" | Select-Object -ExpandProperty FullName
    if ($testSources) {
        & $javac --release 11 -encoding UTF-8 -cp "$projDir\target\classes;$cp" -d "$projDir\target\test-classes" $testSources
        if ($LASTEXITCODE -ne 0) {
            Write-Error "Test compilation failed."
            exit $LASTEXITCODE
        }
        Write-Host "[INFO] Compiled $($testSources.Count) test files to target\test-classes"
    }
}

if ($Goals -contains "test" -or $Goals -contains "package" -or $Goals -contains "verify" -or $Goals -contains "install") {
    Write-Host "[INFO] --- running unit tests ---"
    $testClasses = Get-ChildItem -Path "$projDir\src\test\java" -Recurse -Filter "*Test.java" | Where-Object {
        $_.Name -ne "BossWisePluginTest.java"
    } | ForEach-Object {
        $rel = $_.FullName.Substring("$projDir\src\test\java\".Length)
        $rel.Replace("\", ".").Replace(".java", "")
    }

    if ($testClasses) {
        Write-Host "[INFO] Executing tests: $($testClasses -join ', ')"
        & $java -cp "$projDir\target\test-classes;$projDir\target\classes;$cp" org.junit.runner.JUnitCore $testClasses
        if ($LASTEXITCODE -ne 0) {
            Write-Error "Unit tests failed."
            exit $LASTEXITCODE
        }
    }
}

# Generate JVM argument files for zero-command-length-limit launches without UTF-8 BOM
$fullCp = "$projDir\target\test-classes;$projDir\target\classes;$cp"
$utf8NoBom = New-Object System.Text.UTF8Encoding $false

$jvmArgs = "-ea`n-Dsun.java2d.d3d=false`n-Dsun.java2d.noddraw=true`n-cp`n$fullCp`ncom.bosswise.BossWisePluginTest`n--developer-mode`n--debug"
[System.IO.File]::WriteAllText("$projDir\target\jvm-args.txt", $jvmArgs, $utf8NoBom)

$previewArgs = "-ea`n-Dsun.java2d.d3d=false`n-Dsun.java2d.noddraw=true`n-cp`n$fullCp`ncom.bosswise.BossWisePreviewLauncher"
[System.IO.File]::WriteAllText("$projDir\target\jvm-preview-args.txt", $previewArgs, $utf8NoBom)

if ($Goals -contains "package" -or $Goals -contains "verify" -or $Goals -contains "install") {
    Write-Host "[INFO] --- packaging JAR ---"
    & $jar cf "$projDir\target\bosswise-1.0.0.jar" -C "$projDir\target\classes" .
    if ($LASTEXITCODE -ne 0) {
        Write-Error "JAR packaging failed."
        exit $LASTEXITCODE
    }
    Write-Host "[INFO] Built jar: $projDir\target\bosswise-1.0.0.jar"
}

Write-Host "[INFO] ------------------------------------------------------------------------"
Write-Host "[INFO] BUILD SUCCESS"
Write-Host "[INFO] ------------------------------------------------------------------------"
exit 0
