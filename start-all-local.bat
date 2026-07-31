@echo off
setlocal
cd /d "%~dp0"

echo.
echo  Credit Card Platform - ONE CLICK LOCAL (NO DOCKER)
echo  Finds/downloads Java Maven Node Kafka; uses native Postgres + pgAdmin
echo  Postgres / pgAdmin password: admin
echo.

:: Elevate once so we can reset Postgres password to admin if needed
net session >nul 2>&1
if %errorLevel% neq 0 (
  echo  Requesting Administrator (needed only to set Postgres password=admin)...
  powershell -NoProfile -Command "Start-Process -FilePath '%~f0' -Verb RunAs"
  exit /b 0
)

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\start-all-local.ps1" %*
set EXITCODE=%ERRORLEVEL%

if %EXITCODE% neq 0 (
  echo.
  echo  FAILED with exit code %EXITCODE%. See messages above.
  pause
  exit /b %EXITCODE%
)

echo.
echo  Leave the opened service windows running.
pause
endlocal
