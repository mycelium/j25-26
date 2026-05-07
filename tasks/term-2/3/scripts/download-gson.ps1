$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectDir = Resolve-Path (Join-Path $scriptDir "..")
$libDir = Join-Path $projectDir "lib"
$version = "2.14.0"
$jarName = "gson-$version.jar"
$jarPath = Join-Path $libDir $jarName
$url = "https://repo1.maven.org/maven2/com/google/code/gson/gson/$version/$jarName"

New-Item -ItemType Directory -Force -Path $libDir | Out-Null

if (Test-Path $jarPath) {
    Write-Host "Gson already exists: $jarPath"
    exit 0
}

Write-Host "Downloading Gson $version..."
Write-Host $url
Invoke-WebRequest -Uri $url -OutFile $jarPath
Write-Host "Saved to $jarPath"
