@echo off
echo ===========================================
echo   NIDAR GCS - System Startup
echo ===========================================
echo.

echo 1. Stopping any existing containers...
docker-compose down

echo.
echo 2. Building and Starting System (Background)...
docker-compose up -d --build

echo.
echo 3. Waiting for services to initialize...
timeout /t 15

echo.
echo 4. Opening GCS Dashboard...
start http://localhost

echo.
echo ===========================================
echo   System is Running!
echo   Frontned: http://localhost
echo   Backend:  http://localhost:8080
echo   Database: http://localhost:3307
echo ===========================================
echo.
echo To stop, run 'docker-compose down'
pause
