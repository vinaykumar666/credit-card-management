# Starts all Spring Boot services in separate PowerShell windows (no Docker).
# Prerequisites: PostgreSQL + Kafka on localhost, `mvn clean verify` already done.
# Usage:  .\scripts\run-local-no-docker.ps1

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root

$envBlock = @"
`$env:DB_USERNAME = 'postgres'
`$env:DB_PASSWORD = 'admin'
`$env:OAUTH2_ISSUER = 'http://localhost:8081'
`$env:KAFKA_BOOTSTRAP_SERVERS = 'localhost:9092'
`$env:GATEWAY_BASE_URL = 'http://localhost:8080'
`$env:ENTERPRISE_API_URL = 'http://localhost:8085'
`$env:AUTH_SERVICE_URL = 'http://localhost:8081'
`$env:ACCOUNT_SERVICE_URL = 'http://localhost:8082'
`$env:PAYMENT_SERVICE_URL = 'http://localhost:8083'
`$env:NOTIFICATION_SERVICE_URL = 'http://localhost:8084'
`$env:ENTERPRISE_SERVICE_URL = 'http://localhost:8085'
`$env:BFF_SERVICE_URL = 'http://localhost:8086'
"@

$services = @(
    @{ Name = "auth";          Module = "cards-authentication-service"; Extra = "-am" },
    @{ Name = "account";       Module = "cards-account-details-service"; Extra = "" },
    @{ Name = "enterprise";    Module = "cards-enterprise-api-service"; Extra = "" },
    @{ Name = "payment";       Module = "cards-payment-service"; Extra = "" },
    @{ Name = "notification";  Module = "cards-notification-service"; Extra = "" },
    @{ Name = "gateway";       Module = "cards-api-gateway-service"; Extra = "" },
    @{ Name = "bff";           Module = "cards-bff-dashboard-service"; Extra = "" }
)

Write-Host "Repo: $Root"
Write-Host "Ensure PostgreSQL (5432) and Kafka (9092) are already running."
Write-Host "Starting $($services.Count) services..."

foreach ($svc in $services) {
    $cmd = @"
Set-Location '$Root'
$envBlock
Write-Host '=== $($svc.Name) ($($svc.Module)) ===' -ForegroundColor Cyan
mvn -pl $($svc.Module) $($svc.Extra) spring-boot:run
"@
    Start-Process powershell -ArgumentList @("-NoExit", "-Command", $cmd)
    Start-Sleep -Seconds 2
}

Write-Host ""
Write-Host "All service windows launched."
Write-Host "Wait until Auth (:8081) is UP before using BFF/UI."
Write-Host "Then: cd cards-dashboard-ui; npm start  -> http://localhost:4200"
