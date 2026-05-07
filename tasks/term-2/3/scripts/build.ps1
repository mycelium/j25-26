$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectDir = Resolve-Path (Join-Path $scriptDir "..")
$repoRoot = Resolve-Path (Join-Path $projectDir "..")
$outDir = Join-Path $projectDir "out"
$gsonJar = Join-Path $projectDir "lib\gson-2.14.0.jar"

if (!(Test-Path $gsonJar)) {
    & (Join-Path $scriptDir "download-gson.ps1")
}

if (!(Test-Path $gsonJar)) {
    throw "Gson JAR was not found at $gsonJar. Run 3\scripts\download-gson.ps1 or place gson-2.14.0.jar there manually."
}

if (Test-Path $outDir) {
    Remove-Item -Recurse -Force $outDir
}
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

$sources = @()
$sources += Get-ChildItem -Recurse -Filter *.java -Path (Join-Path $projectDir "src") | ForEach-Object { $_.FullName }
$sources += Get-ChildItem -Recurse -Filter *.java -Path (Join-Path $repoRoot "1\library") | ForEach-Object { $_.FullName }
$sources += Get-ChildItem -Recurse -Filter *.java -Path (Join-Path $repoRoot "2\src\httpserver") | ForEach-Object { $_.FullName }

Write-Host "Compiling task 3 load testing project..."
& javac -encoding UTF-8 -cp $gsonJar -d $outDir @sources
Write-Host "Compiled to $outDir"
