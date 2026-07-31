#Requires -Version 5.1
<#
.SYNOPSIS
  Creates auth_db / account_db / payment_db / notification_db on local Postgres.
  Credentials: user postgres / password admin
#>
$ErrorActionPreference = "Continue"
$DbUser = "postgres"
$DbPassword = "admin"
$HostName = "127.0.0.1"
$Port = 5432

$psql = @(
    "C:\Program Files\PostgreSQL\18\bin\psql.exe",
    "C:\Program Files\PostgreSQL\17\bin\psql.exe",
    "C:\Program Files\PostgreSQL\16\bin\psql.exe",
    "C:\Program Files\PostgreSQL\15\bin\psql.exe",
    "C:\Program Files\PostgreSQL\14\bin\psql.exe"
) | Where-Object { Test-Path $_ } | Select-Object -First 1

if (-not $psql) {
    $cmd = Get-Command psql -ErrorAction SilentlyContinue
    if ($cmd) { $psql = $cmd.Source }
}
if (-not $psql) { throw "psql.exe not found. Install PostgreSQL or add it to PATH." }

Write-Host "Using $psql"
$env:PGPASSWORD = $DbPassword

function Test-Login($user) {
    & $psql -U $user -d postgres -h $HostName -p $Port -v ON_ERROR_STOP=1 -c "SELECT 1" 1>$null 2>$null
    return ($LASTEXITCODE -eq 0)
}

if (-not (Test-Login $DbUser)) {
    Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
    throw @"
Cannot login to Postgres as postgres with password 'admin'.
Fix: run start-all-local.bat as Administrator once (sets password to admin),
or in pgAdmin set password admin for user postgres, then re-run:
  powershell -File scripts\ensure-databases.ps1
"@
}

Write-Host "Logged in as $DbUser"

& $psql -U $DbUser -d postgres -h $HostName -p $Port -v ON_ERROR_STOP=1 -c "ALTER ROLE postgres WITH LOGIN PASSWORD 'admin';"
if ($LASTEXITCODE -ne 0) { throw "Failed to ensure postgres password" }

foreach ($db in @("auth_db", "account_db", "payment_db", "notification_db")) {
    $raw = & $psql -U $DbUser -d postgres -h $HostName -p $Port -tAc "SELECT 1 FROM pg_database WHERE datname='$db'"
    $exists = ("$raw").Trim()
    if ($exists -ne "1") {
        & $psql -U $DbUser -d postgres -h $HostName -p $Port -v ON_ERROR_STOP=1 -c "CREATE DATABASE $db OWNER postgres"
        if ($LASTEXITCODE -ne 0) { throw "Failed to create $db" }
        Write-Host "Created $db" -ForegroundColor Green
    } else {
        Write-Host "Exists  $db" -ForegroundColor Green
    }
}

& $psql -U postgres -d account_db -h $HostName -p $Port -v ON_ERROR_STOP=1 -c "SELECT current_database(), current_user;"
if ($LASTEXITCODE -ne 0) { throw "postgres cannot connect to account_db" }

Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
Write-Host "All databases ready (user=postgres password=admin). Restart Spring Boot services." -ForegroundColor Green
