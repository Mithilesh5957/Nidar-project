@echo off
echo Starting NIDAR Drone Agent...
echo.
echo Select Connection Type:
echo 1. Simulation (Mock Drone)
echo 2. WiFi/UDP (e.g., 192.168.x.x)
echo 3. Telemetry Radio (COM Port)
echo.
set /p choice="Enter choice (1-3): "

if "%choice%"=="1" (
    echo Running in Simulation Mode...
    python drone_agent/main.py --sim
) else if "%choice%"=="2" (
    set /p ip="Enter Drone IP (e.g., 192.168.1.50): "
    set /p port="Enter Port (default 14550): "
    if "%port%"=="" set port=14550
    echo Connecting to udp:%ip%:%port%...
    python drone_agent/main.py --connect udp:%ip%:%port%
) else if "%choice%"=="3" (
    set /p com="Enter COM Port (e.g., COM3): "
    echo Connecting to %com%...
    python drone_agent/main.py --connect %com%
) else (
    echo Invalid choice.
)

pause
