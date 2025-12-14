# NIDAR Field Operations Guide

**Purpose**: Standard Operating Procedure (SOP) for conducting real-time drone missions using the NIDAR GCS.

---

## 📋 1. Equipment Checklist
- [ ] **GCS Laptop** (Windows)
    - [ ] Battery Charged (>50%) or Plugged In.
    - [ ] "Nidar Project" Repository downloaded.
    - [ ] Docker Desktop Running.
- [ ] **Telemetry Radio** (USB Ground Unit)
    - [ ] Antennas secured.
- [ ] **Drone**
    - [ ] Flight Controller (ArduPilot) Powered.
    - [ ] GPS Lock (Blue LED).
    - [ ] Telemetry Radio (Air Unit) Green LED (Solid = Linked).

---

## 🚀 2. System Startup (Ground)

### A. Start the Backend/Dashboard
1.  **Disconnect Internet** (Optional, if operating offline).
2.  Double-click `START_SYSTEM.bat`.
3.  Wait for the terminal to say "Started".
4.  Open Chrome/Edge and go to `http://localhost`.
    - *Verify Map loads.*
    - *Verify "System Status" shows Online.*

### B. Connect Telemetry Radio
1.  Plug the USB Radio into the Laptop.
2.  Open **Device Manager** -> **Ports**. Note the COM port (e.g., `COM3`).

### C. Start the Drone Agent
1.  Double-click `run_agent.bat`.
2.  Select **Option 3** (Telemetry Radio / COM Port).
3.  Enter your Port: `COM3`.
4.  **Confirm Connection**:
    - Console should show: `Heartbeat from system (1, 1)`.
    - Dashboard should show the Drone Icon on the map.

---

## 🚁 3. Pre-Flight Checks (Dashboard)

1.  **Satellite Count**: > 6 Satellites.
2.  **GPS Fix**: "3D Fix" or "RTK Float/Fixed".
3.  **Battery Voltage**: Visible and matching expected voltage (e.g., ~12.6V for 3S).
4.  **Mode**: Stabilize / Loiter.
5.  **Arming Check**:
    - Ensure area is clear.
    - Verify no "Failsafe" warnings in the console.

---

## 🗺️ 4. Mission Execution

### Uploading a Mission
1.  Go to **Mission Planning** panel on Dashboard.
2.  Click map to Set Waypoints.
3.  Click **"Upload Mission"**.
4.  Wait for "Mission Accepted" notification.

### Launch
1.  Switch RC Transmitter to **Loiter** (or verify via Dashboard).
2.  **Arm** the Drone (RC Stick Low-Right).
3.  Takeoff to safe altitude (e.g., 5 meters).
4.  Switch to **Auto** Mode.
5.  *Monitor Dashboard telemetry closely.*

---

## 🚨 5. Emergency Procedures

**Loss of Telemetry (Link Down)**
1.  Do NOT Panic. The drone will continue its Autonomous mission.
2.  Check USB connection of Radio.
3.  If mission completes, it will RTL (Return to Launch).

**Erratic Behavior**
1.  Switch RC Transmitter to **Stabilize** or **Loiter** (Manual Control).
2.  Land immediately.
