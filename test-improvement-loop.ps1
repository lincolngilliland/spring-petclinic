param(
    [int]$MaxIterations = 5,
    [double]$TargetLineCoverage = 90.0,
    [double]$TargetBranchCoverage = 80.0,
    [string]$TestGenerationCommand = '',
    [string]$FailureFixCommand = '',
    [switch]$StopOnTestFailure
)

$ErrorActionPreference = 'Stop'

function Get-MavenWrapper {
    if (Test-Path -Path '.\\mvnw.cmd') {
        return '.\\mvnw.cmd'
    }
    if (Test-Path -Path './mvnw') {
        return './mvnw'
    }
    throw 'Maven wrapper not found. Run this script from spring-petclinic root.'
}

function Invoke-JaCoCoBuild {
    param([string]$Maven)

    & $Maven -q clean test jacoco:report
    return $LASTEXITCODE
}

function Read-CoverageSummary {
    param([string]$JsonPath)

    if (-not (Test-Path -Path $JsonPath)) {
        throw "Coverage JSON output not found at '$JsonPath'."
    }

    $json = Get-Content -Path $JsonPath -Raw | ConvertFrom-Json
    return [pscustomobject]@{
        linePct = [double]$json.summary.line.pct
        branchPct = [double]$json.summary.branch.pct
        methodPct = [double]$json.summary.method.pct
    }
}

$maven = Get-MavenWrapper
$parser = '.\\parse-jacoco-lines.ps1'
if (-not (Test-Path -Path $parser)) {
    throw "Parser script '$parser' was not found."
}

$runDir = 'target\\coverage-loop'
if (-not (Test-Path -Path $runDir)) {
    New-Item -Path $runDir -ItemType Directory -Force | Out-Null
}

$csvPath = Join-Path $runDir 'iterations.csv'
if (-not (Test-Path -Path $csvPath)) {
    'iteration,timestamp,linePct,branchPct,methodPct,status,notes' | Set-Content -Path $csvPath -Encoding utf8
}

for ($i = 1; $i -le $MaxIterations; $i++) {
    Write-Host "`n=== Iteration $i/$MaxIterations ==="

    if ($i -gt 1 -and $TestGenerationCommand) {
        Write-Host "Applying generated test updates..."
        Invoke-Expression $TestGenerationCommand
    }

    $status = 'PASS'
    $notes = ''

    Write-Host 'Running tests and generating JaCoCo report...'
    $exitCode = Invoke-JaCoCoBuild -Maven $maven

    if ($exitCode -ne 0) {
        $status = 'TEST_FAIL'
        $notes = 'Test run failed'

        if ($FailureFixCommand) {
            Write-Host 'Attempting automated failure fix/regeneration...'
            Invoke-Expression $FailureFixCommand

            Write-Host 'Re-running tests after fix attempt...'
            $exitCode = Invoke-JaCoCoBuild -Maven $maven
            if ($exitCode -eq 0) {
                $status = 'PASS_AFTER_FIX'
                $notes = 'Recovered after fix command'
            }
        }

        if ($exitCode -ne 0 -and $StopOnTestFailure) {
            "$i,$((Get-Date).ToString('o')),0,0,0,$status,$notes" | Add-Content -Path $csvPath -Encoding utf8
            throw 'Stopping because test failures remain and -StopOnTestFailure was set.'
        }
    }

    $jsonPath = Join-Path $runDir ("iteration-$i.json")
    . $parser -OutputJsonPath $jsonPath | Out-Null

    $coverage = Read-CoverageSummary -JsonPath $jsonPath
    Write-Host ("Coverage: line={0}% branch={1}% method={2}%" -f $coverage.linePct, $coverage.branchPct, $coverage.methodPct)

    "$i,$((Get-Date).ToString('o')),$($coverage.linePct),$($coverage.branchPct),$($coverage.methodPct),$status,$notes" |
        Add-Content -Path $csvPath -Encoding utf8

    if ($coverage.linePct -ge $TargetLineCoverage -and $coverage.branchPct -ge $TargetBranchCoverage -and $status -notlike 'TEST_FAIL*') {
        Write-Host 'Coverage goals reached and tests are stable.'
        break
    }

    Write-Host 'Coverage target not reached yet. Continue with next iteration.'
}

Write-Host "`nIteration history written to '$csvPath'."
Write-Host "Detailed per-iteration coverage reports are in '$runDir'."