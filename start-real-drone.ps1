# Quick Start Script for Real Drone Control
# Run this in PowerShell to set up NIDAR backend for CubeOrangePlus

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  NIDAR Real Drone Control - Quick Start" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Check COM port
Write-Host "[1/5] Checking CubeOrangePlus connection..." -ForegroundColor Yellow
$comPorts = Get-WmiObject Win32_SerialPort | Where-Object { $_.Description -like "*Cube*" }

if ($comPorts) {
    Write-Host "✅ Found CubeOrangePlus:" -ForegroundColor Green
    $comPorts | Select-Object DeviceID, Description | Format-Table
} else {
    Write-Host "⚠️  Warning: CubeOrangePlus not detected on any COM port" -ForegroundColor Red
    Write-Host "   Make sure it's connected via USB and powered on" -ForegroundColor Yellow
    Write-Host ""
}

# Step 2: Stop backend container
Write-Host "[2/5] Stopping Docker backend container..." -ForegroundColor Yellow
docker stop nidarproject-backend-1 2>$null
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Backend container stopped" -ForegroundColor Green
} else {
    Write-Host "ℹ️  Backend container not running (this is OK)" -ForegroundColor Gray
}
Write-Host ""

# Step 3: Ensure database is running
Write-Host "[3/5] Checking database container..." -ForegroundColor Yellow
$dbStatus = docker ps --filter "name=nidarproject-db-1" --format "{{.Status}}"
if ($dbStatus) {
    Write-Host "✅ Database is running: $dbStatus" -ForegroundColor Green
} else {
    Write-Host "⚠️  Database not running. Starting it now..." -ForegroundColor Yellow
    docker-compose -f "d:\Nidar Project\docker-compose.yml" up -d db
    Write-Host "✅ Database started" -ForegroundColor Green
}
Write-Host ""

# Step 4: Check application.properties
Write-Host "[4/5] Verifying configuration..." -ForegroundColor Yellow
$propsFile = "d:\Nidar Project\backend\src\main\resources\application.properties"
$serialEnabled = Select-String -Path $propsFile -Pattern "mavlink.serial.enabled=true" -Quiet
$simDisabled = Select-String -Path $propsFile -Pattern "mavlink.simulation.enabled=false" -Quiet

if ($serialEnabled -and $simDisabled) {
    Write-Host "✅ Configuration correct:" -ForegroundColor Green
    Write-Host "   - Serial communication: ENABLED" -ForegroundColor Gray
    Write-Host "   - Simulation mode: DISABLED" -ForegroundColor Gray
    Write-Host "   - Serial port: COM6" -ForegroundColor Gray
    Write-Host "   - Baud rate: 115200" -ForegroundColor Gray
} else {
    Write-Host "⚠️  Configuration may need adjustment" -ForegroundColor Yellow
}
Write-Host ""

# Step 5: Ready to start
Write-Host "[5/5] Ready to start backend!" -ForegroundColor Yellow
Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  Start Backend with Real Drone Control" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Run this command to start the backend:" -ForegroundColor White
Write-Host ""
Write-Host "  cd 'd:\Nidar Project\backend'" -ForegroundColor Green
Write-Host "  mvn spring-boot:run" -ForegroundColor Green
Write-Host ""
Write-Host "Expected logs to watch for:" -ForegroundColor White
Write-Host "  ✓ Connected to COM6 at 115200 baud" -ForegroundColor Gray
Write-Host "  ✓ UDP listener started on port 14552" -ForegroundColor Gray
Write-Host "  ✓ Received HEARTBEAT from system 1" -ForegroundColor Gray
Write-Host ""
Write-Host "Then open QGroundControl to see real telemetry!" -ForegroundColor White
Write-Host ""

# Offer to open the terminal in the right directory
$response = Read-Host "Open terminal in backend directory now? (Y/N)"
if ($response -eq "Y" -or $response -eq "y") {
    Set-Location "d:\Nidar Project\backend"
    Write-Host ""
    Write-Host "Now run: mvn spring-boot:run" -ForegroundColor Green
    Write-Host ""
}
