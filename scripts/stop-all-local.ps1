#Requires -Version 5.1
# Stops apps + native Kafka started by start-all-local. Does NOT stop your Windows PostgreSQL service.
$ErrorActionPreference = "Continue"
$Root = Split-Path -Parent $PSScriptRoot
$PidFile = Join-Path $Root ".runtime\service-pids.txt"

Write-Host "==> Stopping tracked processes..." -ForegroundColor Cyan
if (Test-Path $PidFile) {
    Get-Content $PidFile | ForEach-Object {
        if ($_ -match '=(?<id>\d+)$') {
            $id = [int]$Matches['id']
            try {
                # Kill process tree (Maven/java children)
                taskkill /PID $id /T /F 2>$null | Out-Null
                Write-Host "  Stopped PID $id"
            } catch {
                Write-Host "  PID $id already gone"
            }
        }
    }
    Remove-Item $PidFile -Force -ErrorAction SilentlyContinue
} else {
    Write-Host "  No PID file found."
}

Get-CimInstance Win32_Process -ErrorAction SilentlyContinue |
    Where-Object {
        $_.CommandLine -and (
            $_.CommandLine -like "*$Root*spring-boot:run*" -or
            $_.CommandLine -like "*$Root*cards-dashboard-ui*" -or
            $_.CommandLine -like "*kafka-server-start*"
        )
    } |
    ForEach-Object {
        taskkill /PID $_.ProcessId /T /F 2>$null | Out-Null
        Write-Host "  Stopped $($_.Name) PID $($_.ProcessId)"
    }

Write-Host "Postgres Windows service left running (native)." -ForegroundColor Yellow
Write-Host "Done." -ForegroundColor Green
