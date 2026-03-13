param(
    [string]$JacocoXmlPath = 'target\site\jacoco\jacoco.xml',
    [int]$Top = 40,
    [string]$OutputJsonPath
)

if (-not (Test-Path -Path $JacocoXmlPath)) {
    throw "JaCoCo XML not found at '$JacocoXmlPath'. Run tests with jacoco:report first."
}

[xml]$xml = Get-Content -Path $JacocoXmlPath

function Get-CounterMap {
    param([object[]]$Counters)

    $map = @{}
    foreach ($counter in $Counters) {
        $type = [string]$counter.type
        $missed = [int]$counter.missed
        $covered = [int]$counter.covered
        $total = $missed + $covered
        $pct = if ($total -eq 0) { 100.0 } else { [math]::Round((100.0 * $covered) / $total, 2) }
        $map[$type] = [pscustomobject]@{
            missed = $missed
            covered = $covered
            total = $total
            pct = $pct
        }
    }
    return $map
}

$reportCounters = Get-CounterMap -Counters $xml.report.counter

$lineGaps = @()
foreach ($pkg in $xml.report.package) {
    $packageName = [string]$pkg.name
    foreach ($sf in $pkg.sourcefile) {
        $sourceFile = [string]$sf.name
        foreach ($line in $sf.line) {
            $missedInstructions = [int]$line.mi
            $missedBranches = [int]$line.mb
            if ($missedInstructions -gt 0 -or $missedBranches -gt 0) {
                $lineGaps += [pscustomobject]@{
                    package = $packageName
                    sourceFile = $sourceFile
                    line = [int]$line.nr
                    missedInstructions = $missedInstructions
                    coveredInstructions = [int]$line.ci
                    missedBranches = $missedBranches
                    coveredBranches = [int]$line.cb
                    score = ($missedInstructions * 2) + ($missedBranches * 3)
                }
            }
        }
    }
}

$topLineGaps = $lineGaps |
    Sort-Object -Property @{ Expression = 'score'; Descending = $true },
                           @{ Expression = 'missedBranches'; Descending = $true },
                           @{ Expression = 'missedInstructions'; Descending = $true } |
    Select-Object -First $Top

$fileSummaries = $lineGaps |
    Group-Object -Property package, sourceFile |
    ForEach-Object {
        $first = $_.Group[0]
        [pscustomobject]@{
            package = $first.package
            sourceFile = $first.sourceFile
            uncoveredLines = $_.Count
            missedInstructions = ($_.Group | Measure-Object -Property missedInstructions -Sum).Sum
            missedBranches = ($_.Group | Measure-Object -Property missedBranches -Sum).Sum
        }
    } |
    Sort-Object -Property @{ Expression = 'missedBranches'; Descending = $true },
                           @{ Expression = 'missedInstructions'; Descending = $true },
                           @{ Expression = 'uncoveredLines'; Descending = $true }

$result = [pscustomobject]@{
    generatedAt = (Get-Date).ToString('o')
    jacocoXmlPath = $JacocoXmlPath
    summary = [pscustomobject]@{
        line = $reportCounters['LINE']
        branch = $reportCounters['BRANCH']
        method = $reportCounters['METHOD']
        class = $reportCounters['CLASS']
        instruction = $reportCounters['INSTRUCTION']
    }
    topLineGaps = $topLineGaps
    fileSummaries = $fileSummaries
}

Write-Host 'JaCoCo summary:'
Write-Host ("  LINE   : {0}% ({1}/{2})" -f $result.summary.line.pct, $result.summary.line.covered, $result.summary.line.total)
Write-Host ("  BRANCH : {0}% ({1}/{2})" -f $result.summary.branch.pct, $result.summary.branch.covered, $result.summary.branch.total)
Write-Host ("  METHOD : {0}% ({1}/{2})" -f $result.summary.method.pct, $result.summary.method.covered, $result.summary.method.total)

if ($topLineGaps.Count -gt 0) {
    Write-Host "Top uncovered lines (up to $Top):"
    foreach ($gap in $topLineGaps) {
        Write-Host ("  {0}/{1}:{2}  mi={3} mb={4}" -f $gap.package, $gap.sourceFile, $gap.line, $gap.missedInstructions, $gap.missedBranches)
    }
} else {
    Write-Host 'No uncovered lines found in report.'
}

if ($OutputJsonPath) {
    $dir = Split-Path -Parent $OutputJsonPath
    if ($dir -and -not (Test-Path -Path $dir)) {
        New-Item -Path $dir -ItemType Directory -Force | Out-Null
    }
    $result | ConvertTo-Json -Depth 8 | Set-Content -Path $OutputJsonPath -Encoding utf8
    Write-Host "Wrote detailed JSON output to '$OutputJsonPath'."
}
