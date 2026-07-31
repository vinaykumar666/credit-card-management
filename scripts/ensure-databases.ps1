#Requires -Version 5.1
<#
.SYNOPSIS
  Creates auth_db / account_db / payment_db / notification_db on local Postgres.
  Uses user cards / password admin (same as start-all-local.bat).
#>
$ErrorActionPreference = "Continue"
$DbUser = "cards"
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

$super = $null
if (Test-Login $DbUser) { $super = $DbUser }
elseif (Test-Login "postgres") { $super = "postgres" }
else {
    Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
    throw @"
Cannot login to Postgres as cards or postgres with password 'admin'.
Fix: run start-all-local.bat as Administrator once (sets password to admin),
or in pgAdmin set password admin for user postgres/cards, then re-run this script:
  powershell -File scripts\ensure-databases.ps1
"@
}

Write-Host "Logged in as $super"

& $psql -U $super -d postgres -h $HostName -p $Port -v ON_ERROR_STOP=1 -c @"
DO `$`$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'cards') THEN
    CREATE ROLE cards LOGIN PASSWORD 'admin' CREATEDB;
  ELSE
    ALTER ROLE cards WITH LOGIN PASSWORD 'admin' CREATEDB;
  END IF;
END
`$`$;
"@
if ($LASTEXITCODE -ne 0) { throw "Failed to ensure role cards" }

foreach ($db in @("auth_db", "account_db", "payment_db", "notification_db")) {
    $raw = & $psql -U $super -d postgres -h $HostName -p $Port -tAc "SELECT 1 FROM pg_database WHERE datname='$db'"
    $exists = ("$raw").Trim()
    if ($exists -ne "1") {
        & $psql -U $super -d postgres -h $HostName -p $Port -v ON_ERROR_STOP=1 -c "CREATE DATABASE $db OWNER cards"
        if ($LASTEXITCODE -ne 0) { throw "Failed to create $db" }
        Write-Host "Created $db" -ForegroundColor Green
    } else {
        Write-Host "Exists  $db" -ForegroundColor Green
    }
}

# Verify app login into account_db
$env:PGPASSWORD = $DbPassword
& $psql -U cards -d account_db -h $HostName -p $Port -v ON_ERROR_STOP=1 -c "SELECT current_database(), current_user;"
if ($LASTEXITCODE -ne 0) { throw "cards cannot connect to account_db" }

Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
Write-Host "All databases ready. Restart the failing Spring Boot service." -ForegroundColor Green
