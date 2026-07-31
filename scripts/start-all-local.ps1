#Requires -Version 5.1
<#
.SYNOPSIS
  One-click LOCAL start with NO Docker: find/download tools, native Postgres/Kafka/pgAdmin, all apps.

.NOTES
  Double-click start-all-local.bat (Run as Administrator recommended once for Postgres password).
  Postgres: cards / admin on localhost:5432
  pgAdmin master / DB password: admin
#>
param(
    [switch]$SkipBuild,
    [switch]$InfraOnly
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root

$Runtime = Join-Path $Root ".runtime"
$Tools = Join-Path $Runtime "tools"
$Logs = Join-Path $Runtime "logs"
$PidFile = Join-Path $Runtime "service-pids.txt"
$KafkaHome = Join-Path $Tools "kafka"
$KafkaData = Join-Path $Runtime "kafka-data"
$KafkaMarker = Join-Path $Runtime "kafka-formatted.ok"
New-Item -ItemType Directory -Force -Path $Tools, $Logs, $KafkaData | Out-Null

$DbUser = "cards"
$DbPassword = "admin"
$UiUrl = "http://localhost:4200"
$script:PsqlExe = $null
$script:PgCtlExe = $null
$script:PgDataDir = $null
$script:PgHba = $null
$script:PgService = $null

function Write-Step($msg) { Write-Host "`n==> $msg" -ForegroundColor Cyan }
function Write-Ok($msg) { Write-Host "  OK: $msg" -ForegroundColor Green }
function Write-Warn($msg) { Write-Host "  WARN: $msg" -ForegroundColor Yellow }

function Test-Command($name) {
    return [bool](Get-Command $name -ErrorAction SilentlyContinue)
}

# Native CLIs (java -version, etc.) write to stderr; with $ErrorActionPreference=Stop that becomes a terminating error.
function Invoke-NativeText([scriptblock]$Command) {
    $prev = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    try {
        $output = & $Command 2>&1 | ForEach-Object { "$_" }
        return ($output -join [Environment]::NewLine)
    } finally {
        $ErrorActionPreference = $prev
    }
}

function Add-PathFront($dir) {
    if (-not $dir -or -not (Test-Path $dir)) { return }
    if ($env:Path -notlike "*$dir*") { $env:Path = "$dir;$env:Path" }
}

function Expand-Zip($zip, $dest) {
    if (Test-Path $dest) { Remove-Item -Recurse -Force $dest }
    New-Item -ItemType Directory -Force -Path $dest | Out-Null
    Expand-Archive -Path $zip -DestinationPath $dest -Force
}

function Get-FirstChildDir($path) {
    return (Get-ChildItem -Path $path -Directory | Select-Object -First 1).FullName
}

function Test-Admin {
    $id = [Security.Principal.WindowsIdentity]::GetCurrent()
    $p = New-Object Security.Principal.WindowsPrincipal($id)
    return $p.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
}

function Ensure-Java {
    Write-Step "Checking Java 21+"
    if (Test-Command "java") {
        $v = Invoke-NativeText { java -version }
        if ($v -match '"(\d+)' -and [int]$Matches[1] -ge 21) {
            Write-Ok "Found Java $($Matches[1]) on PATH"
            return
        }
    }
    $jdkHome = Join-Path $Tools "jdk-21"
    $javaExe = Join-Path $jdkHome "bin\java.exe"
    if (-not (Test-Path $javaExe)) {
        Write-Warn "Downloading Eclipse Temurin JDK 21 (portable into .runtime)..."
        $zip = Join-Path $Runtime "jdk21.zip"
        $url = "https://api.adoptium.net/v3/binary/latest/21/ga/windows/x64/jdk/hotspot/normal/eclipse?project=jdk"
        Invoke-WebRequest -Uri $url -OutFile $zip -UseBasicParsing
        $extract = Join-Path $Runtime "jdk-extract"
        Expand-Zip $zip $extract
        $inner = Get-FirstChildDir $extract
        if (Test-Path $jdkHome) { Remove-Item -Recurse -Force $jdkHome }
        Move-Item $inner $jdkHome
        Remove-Item -Recurse -Force $extract, $zip -ErrorAction SilentlyContinue
    }
    $env:JAVA_HOME = $jdkHome
    Add-PathFront (Join-Path $jdkHome "bin")
    Write-Ok "Using portable JDK at $jdkHome"
}

function Ensure-Maven {
    Write-Step "Checking Maven"
    if (Test-Command "mvn") { Write-Ok "Found mvn on PATH"; return }
    $mvnHome = Join-Path $Tools "apache-maven-3.9.9"
    $mvnCmd = Join-Path $mvnHome "bin\mvn.cmd"
    if (-not (Test-Path $mvnCmd)) {
        Write-Warn "Downloading Apache Maven 3.9.9..."
        $zip = Join-Path $Runtime "maven.zip"
        $urls = @(
            "https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip",
            "https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip"
        )
        $ok = $false
        foreach ($url in $urls) {
            try { Invoke-WebRequest -Uri $url -OutFile $zip -UseBasicParsing; $ok = $true; break } catch {}
        }
        if (-not $ok) { throw "Could not download Maven." }
        $extract = Join-Path $Runtime "maven-extract"
        Expand-Zip $zip $extract
        $inner = Get-FirstChildDir $extract
        if (Test-Path $mvnHome) { Remove-Item -Recurse -Force $mvnHome }
        Move-Item $inner $mvnHome
        Remove-Item -Recurse -Force $extract, $zip -ErrorAction SilentlyContinue
    }
    Add-PathFront (Join-Path $mvnHome "bin")
    Write-Ok "Using portable Maven at $mvnHome"
}

function Ensure-Node {
    Write-Step "Checking Node.js 20+"
    if (Test-Command "node") {
        $major = [int]((& node -v) -replace '[^0-9].*', '')
        if ($major -ge 20) { Write-Ok "Found Node $((node -v)) on PATH"; return }
    }
    $nodeHome = Join-Path $Tools "node-v20.18.1-win-x64"
    if (-not (Test-Path (Join-Path $nodeHome "node.exe"))) {
        Write-Warn "Downloading Node.js 20.18.1 portable..."
        $zip = Join-Path $Runtime "node.zip"
        Invoke-WebRequest -Uri "https://nodejs.org/dist/v20.18.1/node-v20.18.1-win-x64.zip" -OutFile $zip -UseBasicParsing
        $extract = Join-Path $Runtime "node-extract"
        Expand-Zip $zip $extract
        $inner = Get-FirstChildDir $extract
        if (Test-Path $nodeHome) { Remove-Item -Recurse -Force $nodeHome }
        Move-Item $inner $nodeHome
        Remove-Item -Recurse -Force $extract, $zip -ErrorAction SilentlyContinue
    }
    Add-PathFront $nodeHome
    Write-Ok "Using portable Node at $nodeHome"
}

function Find-Postgres {
    Write-Step "Looking for native PostgreSQL (no Docker)"
    $svc = Get-CimInstance Win32_Service -Filter "Name LIKE 'postgresql%'" -ErrorAction SilentlyContinue |
        Select-Object -First 1
    if ($svc -and $svc.PathName -match '"([^"]+pg_ctl\.exe)"') {
        $script:PgCtlExe = $Matches[1]
        $script:PsqlExe = Join-Path (Split-Path $script:PgCtlExe) "psql.exe"
        $script:PgService = $svc.Name
        if ($svc.PathName -match '-D\s+"([^"]+)"') { $script:PgDataDir = $Matches[1] }
        elseif ($svc.PathName -match '-D\s+(\S+)') { $script:PgDataDir = $Matches[1] }
    }

    if (-not $script:PsqlExe -or -not (Test-Path $script:PsqlExe)) {
        $candidates = @(
            "C:\Program Files\PostgreSQL\18\bin\psql.exe",
            "C:\Program Files\PostgreSQL\17\bin\psql.exe",
            "C:\Program Files\PostgreSQL\16\bin\psql.exe",
            "C:\Program Files\PostgreSQL\15\bin\psql.exe"
        )
        foreach ($c in $candidates) {
            if (Test-Path $c) {
                $script:PsqlExe = $c
                $script:PgCtlExe = Join-Path (Split-Path $c) "pg_ctl.exe"
                $ver = Split-Path (Split-Path (Split-Path $c)) -Leaf
                $script:PgDataDir = "C:\Program Files\PostgreSQL\$ver\data"
                $script:PgService = "postgresql-x64-$ver"
                break
            }
        }
    }

    if (-not $script:PsqlExe -or -not (Test-Path $script:PsqlExe)) {
        Write-Warn "PostgreSQL not found — installing via winget (PostgreSQL 16)..."
        if (-not (Test-Command "winget")) {
            throw "Install PostgreSQL from https://www.postgresql.org/download/windows/ (password=admin), then re-run."
        }
        $prev = $ErrorActionPreference
        $ErrorActionPreference = "Continue"
        winget install -e --id PostgreSQL.PostgreSQL.16 --accept-package-agreements --accept-source-agreements --disable-interactivity
        $ErrorActionPreference = $prev
        Start-Sleep -Seconds 5
        Find-Postgres
        if (-not $script:PsqlExe -or -not (Test-Path $script:PsqlExe)) {
            throw "PostgreSQL install finished but psql.exe was not found. Re-open this script after install completes."
        }
        return
    }

    Add-PathFront (Split-Path $script:PsqlExe)
    if ($script:PgDataDir) { $script:PgHba = Join-Path $script:PgDataDir "pg_hba.conf" }
    Write-Ok "psql: $($script:PsqlExe)"
    if ($script:PgService) { Write-Ok "service: $($script:PgService)" }
}

function Start-PostgresService {
    if (-not $script:PgService) { return }
    $s = Get-Service -Name $script:PgService -ErrorAction SilentlyContinue
    if (-not $s) { return }
    if ($s.Status -ne "Running") {
        Write-Warn "Starting $($script:PgService)..."
        Start-Service $script:PgService
        Start-Sleep -Seconds 3
    }
    Write-Ok "PostgreSQL service is running"
}

function Test-PgLogin([string]$user, [string]$password, [string]$db = "postgres") {
    $env:PGPASSWORD = $password
    $prev = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    & $script:PsqlExe -U $user -d $db -h 127.0.0.1 -p 5432 -v ON_ERROR_STOP=1 -c "SELECT 1" 1>$null 2>$null
    $ok = ($LASTEXITCODE -eq 0)
    $ErrorActionPreference = $prev
    Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
    return $ok
}

function Enable-PgTrust {
    if (-not $script:PgHba -or -not (Test-Path $script:PgHba)) {
        throw "Cannot find pg_hba.conf to set password=admin. Run this BAT as Administrator once."
    }
    if (-not (Test-Admin)) {
        throw "Need Administrator once to set Postgres password to 'admin'. Right-click start-all-local.bat -> Run as administrator."
    }
    $backup = "$($script:PgHba).cards.bak"
    if (-not (Test-Path $backup)) { Copy-Item $script:PgHba $backup -Force }
    $lines = Get-Content $script:PgHba
    $new = foreach ($line in $lines) {
        if ($line -match '^\s*#' -or $line -match '^\s*$') { $line }
        elseif ($line -match '^\s*(local|host)\s+') {
            ($line -replace '\b(scram-sha-256|md5|password|reject)\b', 'trust')
        } else { $line }
    }
    Set-Content -Path $script:PgHba -Value $new -Encoding ascii
    Restart-Service $script:PgService -Force
    Start-Sleep -Seconds 4
}

function Restore-PgHba {
    $backup = "$($script:PgHba).cards.bak"
    if ((Test-Path $backup) -and (Test-Path $script:PgHba)) {
        Copy-Item $backup $script:PgHba -Force
        if ($script:PgService) {
            $prev = $ErrorActionPreference
            $ErrorActionPreference = "Continue"
            & $script:PgCtlExe reload -D $script:PgDataDir 1>$null 2>$null
            if ($LASTEXITCODE -ne 0) { Restart-Service $script:PgService -Force; Start-Sleep -Seconds 3 }
            $ErrorActionPreference = $prev
        }
    }
}

function Ensure-PostgresAuthAndDbs {
    Write-Step "Configuring Postgres user/password = cards / admin"
    Start-PostgresService

    $loginOk = (Test-PgLogin $DbUser $DbPassword "postgres") -or (Test-PgLogin "postgres" $DbPassword "postgres")
    if (-not $loginOk) {
        Write-Warn "Cannot login with password 'admin' — temporarily enabling local trust to reset..."
        Enable-PgTrust
        try {
            $env:PGPASSWORD = ""
            $sql = @"
DO `$`$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'cards') THEN
    CREATE ROLE cards LOGIN PASSWORD 'admin' SUPERUSER CREATEDB;
  ELSE
    ALTER ROLE cards WITH LOGIN PASSWORD 'admin' SUPERUSER CREATEDB;
  END IF;
  IF EXISTS (SELECT FROM pg_roles WHERE rolname = 'postgres') THEN
    ALTER ROLE postgres WITH PASSWORD 'admin';
  END IF;
END
`$`$;
"@
            & $script:PsqlExe -U postgres -d postgres -h 127.0.0.1 -p 5432 -v ON_ERROR_STOP=1 -c $sql
            if ($LASTEXITCODE -ne 0) {
                & $script:PsqlExe -U cards -d postgres -h 127.0.0.1 -p 5432 -v ON_ERROR_STOP=1 -c $sql
            }
        } finally {
            Restore-PgHba
        }
        if (-not (Test-PgLogin $DbUser $DbPassword "postgres")) {
            throw "Failed to set Postgres password to admin."
        }
        Write-Ok "Password reset to admin"
    } else {
        Write-Ok "Login with password admin works"
        # Ensure cards role exists with admin password
        $env:PGPASSWORD = $DbPassword
        $super = if (Test-PgLogin "postgres" $DbPassword "postgres") { "postgres" } else { $DbUser }
        $sql = @"
DO `$`$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'cards') THEN
    CREATE ROLE cards LOGIN PASSWORD 'admin' SUPERUSER CREATEDB;
  ELSE
    ALTER ROLE cards WITH LOGIN PASSWORD 'admin' SUPERUSER CREATEDB;
  END IF;
END
`$`$;
"@
        & $script:PsqlExe -U $super -d postgres -h 127.0.0.1 -p 5432 -v ON_ERROR_STOP=1 -c $sql 1>$null
        Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
    }

    Write-Step "Ensuring application databases"
    $env:PGPASSWORD = $DbPassword
    foreach ($db in @("auth_db", "account_db", "payment_db", "notification_db")) {
        $exists = & $script:PsqlExe -U $DbUser -d postgres -h 127.0.0.1 -tAc "SELECT 1 FROM pg_database WHERE datname='$db'"
        if ("$exists".Trim() -ne "1") {
            & $script:PsqlExe -U $DbUser -d postgres -h 127.0.0.1 -v ON_ERROR_STOP=1 -c "CREATE DATABASE $db OWNER cards"
            Write-Ok "Created $db"
        } else {
            Write-Ok "$db already exists"
        }
    }
    Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
}

function Test-PortOpen([int]$port) {
    try {
        $c = New-Object System.Net.Sockets.TcpClient
        $c.Connect("127.0.0.1", $port)
        $c.Close()
        return $true
    } catch { return $false }
}

function Ensure-Kafka {
    Write-Step "Native Kafka on localhost:9092 (no Docker)"
    if (Test-PortOpen 9092) {
        Write-Ok "Port 9092 already open — reusing existing broker"
        return
    }

    Ensure-Java
    $serverProps = Join-Path $KafkaHome "config\kraft\server.properties"
    if (-not (Test-Path $serverProps)) {
        Write-Warn "Downloading Apache Kafka 3.7.2..."
        $tgz = Join-Path $Runtime "kafka.tgz"
        $url = "https://archive.apache.org/dist/kafka/3.7.2/kafka_2.13-3.7.2.tgz"
        Invoke-WebRequest -Uri $url -OutFile $tgz -UseBasicParsing
        $extract = Join-Path $Runtime "kafka-extract"
        if (Test-Path $extract) { Remove-Item -Recurse -Force $extract }
        New-Item -ItemType Directory -Force -Path $extract | Out-Null
        tar -xzf $tgz -C $extract
        if ($LASTEXITCODE -ne 0) { throw "Failed to extract Kafka (tar)." }
        $inner = Get-FirstChildDir $extract
        if (Test-Path $KafkaHome) { Remove-Item -Recurse -Force $KafkaHome }
        Move-Item $inner $KafkaHome
        Remove-Item -Recurse -Force $extract, $tgz -ErrorAction SilentlyContinue
    }

    $logDirUnix = ($KafkaData -replace '\\', '/')
    $propsPath = Join-Path $KafkaHome "config\kraft\server.properties"
    $props = Get-Content $propsPath -Raw
    if ($props -notmatch [regex]::Escape($logDirUnix)) {
        $props = $props -replace '(?m)^log\.dirs=.*$', "log.dirs=$logDirUnix"
        if ($props -notmatch '(?m)^listeners=') {
            $props += "`r`nlisteners=PLAINTEXT://localhost:9092`r`n"
        } else {
            $props = $props -replace '(?m)^listeners=.*$', 'listeners=PLAINTEXT://localhost:9092'
        }
        if ($props -match '(?m)^advertised\.listeners=') {
            $props = $props -replace '(?m)^advertised\.listeners=.*$', 'advertised.listeners=PLAINTEXT://localhost:9092'
        } else {
            $props += "advertised.listeners=PLAINTEXT://localhost:9092`r`n"
        }
        Set-Content -Path $propsPath -Value $props -Encoding ascii
    }

    $storageBat = Join-Path $KafkaHome "bin\windows\kafka-storage.bat"
    $startBat = Join-Path $KafkaHome "bin\windows\kafka-server-start.bat"
    if (-not (Test-Path $storageBat)) { throw "Kafka Windows scripts missing under $KafkaHome" }

    if (-not (Test-Path $KafkaMarker)) {
        Write-Host "  Formatting KRaft storage (one-time)..."
        Push-Location $KafkaHome
        try {
            $uuid = (& cmd /c "bin\windows\kafka-storage.bat random-uuid").Trim()
            if (-not $uuid) { throw "Could not generate Kafka cluster id." }
            cmd /c "bin\windows\kafka-storage.bat format -t $uuid -c config\kraft\server.properties"
            if ($LASTEXITCODE -ne 0) { throw "Kafka storage format failed." }
            Set-Content -Path $KafkaMarker -Value $uuid
        } finally { Pop-Location }
    }

    $kafkaLog = Join-Path $Logs "kafka.log"
    $cmd = @"
Set-Location '$KafkaHome'
`$env:JAVA_HOME = '$($env:JAVA_HOME)'
`$env:Path = '$($env:Path)'
Write-Host '=== Kafka (native KRaft) :9092 ===' -ForegroundColor Cyan
cmd /c "bin\windows\kafka-server-start.bat config\kraft\server.properties" *>&1 | Tee-Object -FilePath '$kafkaLog'
"@
    $p = Start-Process powershell -PassThru -ArgumentList @("-NoExit", "-Command", $cmd)
    Add-Content -Path $PidFile -Value "kafka=$($p.Id)"

    Write-Host "  Waiting for Kafka :9092..."
    $ready = $false
    for ($i = 0; $i -lt 90; $i++) {
        if (Test-PortOpen 9092) { $ready = $true; break }
        Start-Sleep -Seconds 2
    }
    if (-not $ready) { throw "Kafka did not open port 9092. See $kafkaLog" }
    Write-Ok "Kafka listening on localhost:9092 (PID $($p.Id))"
}

function Ensure-PgAdmin {
    Write-Step "pgAdmin (native desktop) — password admin"
    $pgAdmin = @(
        "C:\Program Files\pgAdmin 4\runtime\pgAdmin4.exe",
        "C:\Program Files\pgAdmin 4\bin\pgAdmin4.exe",
        "${env:ProgramFiles(x86)}\pgAdmin 4\runtime\pgAdmin4.exe",
        "$env:LOCALAPPDATA\Programs\pgAdmin 4\runtime\pgAdmin4.exe"
    ) | Where-Object { Test-Path $_ } | Select-Object -First 1

    if (-not $pgAdmin) {
        if (Test-Command "winget") {
            Write-Warn "Installing pgAdmin 4 via winget..."
            $prev = $ErrorActionPreference
            $ErrorActionPreference = "Continue"
            winget install -e --id PostgreSQL.pgAdmin --accept-package-agreements --accept-source-agreements --disable-interactivity
            $ErrorActionPreference = $prev
            Start-Sleep -Seconds 3
            $pgAdmin = @(
                "C:\Program Files\pgAdmin 4\runtime\pgAdmin4.exe",
                "C:\Program Files\pgAdmin 4\bin\pgAdmin4.exe",
                "$env:LOCALAPPDATA\Programs\pgAdmin 4\runtime\pgAdmin4.exe"
            ) | Where-Object { Test-Path $_ } | Select-Object -First 1
        }
    }

    if ($pgAdmin) {
        Start-Process $pgAdmin | Out-Null
        Write-Ok "Launched pgAdmin: $pgAdmin"
        Write-Ok "Master password / DB password: admin"
        Write-Ok "Register server: Host=localhost Port=5432 User=cards Password=admin"
    } else {
        Write-Warn "pgAdmin not installed. Optional: winget install PostgreSQL.pgAdmin"
        Write-Warn "Or use any SQL client with cards / admin @ localhost:5432"
    }
}

function Invoke-MavenBuild {
    if ($SkipBuild) { Write-Warn "Skipping Maven build (-SkipBuild)"; return }
    Write-Step "Building all Java modules (mvn clean install -DskipTests)"
    & mvn -q clean install "-DskipTests"
    if ($LASTEXITCODE -ne 0) { throw "Maven build failed." }
    Write-Ok "Maven build complete"
}

function Get-CommonEnvBlock {
    return @"
`$env:DB_USERNAME = '$DbUser'
`$env:DB_PASSWORD = '$DbPassword'
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
`$env:JAVA_HOME = '$($env:JAVA_HOME)'
`$env:Path = '$($env:Path)'
"@
}

function Start-AllApps {
    Write-Step "Starting Spring Boot services + Angular UI"
    if (Test-Path $PidFile) {
        # keep kafka pid line if present
        $keep = @(Get-Content $PidFile | Where-Object { $_ -like "kafka=*" })
        Set-Content $PidFile -Value $keep
    } else {
        New-Item -ItemType File -Path $PidFile -Force | Out-Null
    }

    $envBlock = Get-CommonEnvBlock
    $services = @(
        @{ Name = "auth";         Module = "cards-authentication-service"; Extra = "-am"; Delay = 0 },
        @{ Name = "account";      Module = "cards-account-details-service"; Extra = ""; Delay = 3 },
        @{ Name = "enterprise";   Module = "cards-enterprise-api-service"; Extra = ""; Delay = 3 },
        @{ Name = "payment";      Module = "cards-payment-service"; Extra = ""; Delay = 5 },
        @{ Name = "notification"; Module = "cards-notification-service"; Extra = ""; Delay = 3 },
        @{ Name = "gateway";      Module = "cards-api-gateway-service"; Extra = ""; Delay = 8 },
        @{ Name = "bff";          Module = "cards-bff-dashboard-service"; Extra = ""; Delay = 5 }
    )

    foreach ($svc in $services) {
        if ($svc.Delay -gt 0) { Start-Sleep -Seconds $svc.Delay }
        $log = Join-Path $Logs "$($svc.Name).log"
        $cmd = @"
Set-Location '$Root'
$envBlock
Write-Host '=== $($svc.Name) ($($svc.Module)) ===' -ForegroundColor Cyan
mvn -pl $($svc.Module) $($svc.Extra) spring-boot:run *>&1 | Tee-Object -FilePath '$log'
"@
        $p = Start-Process powershell -PassThru -ArgumentList @("-NoExit", "-Command", $cmd)
        Add-Content -Path $PidFile -Value "$($svc.Name)=$($p.Id)"
        Write-Ok "Launched $($svc.Name) (PID $($p.Id))"
    }

    Write-Step "Installing / starting Angular UI"
    $uiDir = Join-Path $Root "cards-dashboard-ui"
    $uiLog = Join-Path $Logs "ui.log"
    $uiCmd = @"
Set-Location '$uiDir'
`$env:Path = '$($env:Path)'
if (-not (Test-Path 'node_modules')) { npm install }
Write-Host '=== Angular UI :4200 ===' -ForegroundColor Cyan
npm start *>&1 | Tee-Object -FilePath '$uiLog'
"@
    $ui = Start-Process powershell -PassThru -ArgumentList @("-NoExit", "-Command", $uiCmd)
    Add-Content -Path $PidFile -Value "ui=$($ui.Id)"
    Write-Ok "Launched UI (PID $($ui.Id))"
}

function Wait-ForHttp($url, $seconds = 180) {
    $deadline = (Get-Date).AddSeconds($seconds)
    while ((Get-Date) -lt $deadline) {
        try {
            $r = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 3 -ErrorAction Stop
            if ($r.StatusCode -ge 200 -and $r.StatusCode -lt 500) { return $true }
        } catch {}
        Start-Sleep -Seconds 3
    }
    return $false
}

function Show-Summary {
    Write-Step "Waiting for Auth (:8081) and UI (:4200)"
    $authUp = Wait-ForHttp "http://localhost:8081/actuator/health" 240
    if (-not $authUp) { $authUp = Wait-ForHttp "http://localhost:8081/.well-known/openid-configuration" 60 }
    $uiUp = Wait-ForHttp $UiUrl 240

    Write-Host ""
    Write-Host "============================================" -ForegroundColor Green
    Write-Host "  LOCAL READY (NO DOCKER)" -ForegroundColor Green
    Write-Host "============================================" -ForegroundColor Green
    Write-Host "  UI:        $UiUrl"
    Write-Host "  BFF:       http://localhost:8086"
    Write-Host "  Gateway:   http://localhost:8080"
    Write-Host "  Auth:      http://localhost:8081"
    Write-Host "  Postgres:  localhost:5432  user=cards  pass=admin"
    Write-Host "  pgAdmin:   desktop app — password admin"
    Write-Host "  Kafka:     localhost:9092  (native)"
    Write-Host "  Login UI:  ada.lovelace@cards.local / Password123!"
    Write-Host "  Logs:      $Logs"
    Write-Host "  Stop apps: stop-all-local.bat"
    Write-Host "============================================" -ForegroundColor Green

    if ($uiUp) { Start-Process $UiUrl }
}

# --------------- main ---------------
Write-Host "Repo: $Root" -ForegroundColor White
Write-Host "Native local start — NO Docker containers" -ForegroundColor White

if (Test-Path $PidFile) { Remove-Item $PidFile -Force }

Ensure-Java
Ensure-Maven
Ensure-Node
Find-Postgres
Ensure-PostgresAuthAndDbs
Ensure-Kafka
Ensure-PgAdmin

if ($InfraOnly) {
    Write-Ok "Infra-only mode complete (Postgres + Kafka + pgAdmin)."
    exit 0
}

Invoke-MavenBuild
Start-AllApps
Show-Summary
