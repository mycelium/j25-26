param(
    [string]$HostName = "127.0.0.1",
    [int]$BasePort = 18080,
    [int]$ServerThreads = 12,
    [int]$ClientThreads = 64,
    [int]$WarmupRequests = 500,
    [int]$Requests = 5000,
    [int]$Repeats = 3
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectDir = Resolve-Path (Join-Path $scriptDir "..")
$gsonJar = Join-Path $projectDir "lib\gson-2.14.0.jar"
$outDir = Join-Path $projectDir "out"

& (Join-Path $scriptDir "build.ps1")

$classpath = "$outDir;$gsonJar"
& java -cp $classpath loadtest.RunAllBenchmarks `
    --host $HostName `
    --base-port $BasePort `
    --server-threads $ServerThreads `
    --client-threads $ClientThreads `
    --warmup-requests $WarmupRequests `
    --requests $Requests `
    --repeats $Repeats `
    --runtime-dir (Join-Path $projectDir "runtime") `
    --results-dir (Join-Path $projectDir "results") `
    --readme (Join-Path $projectDir "README.md")
