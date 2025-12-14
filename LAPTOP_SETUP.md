# Laptop Setup Guide (Windows)

This guide explains how to set up your Windows Laptop to run the **Drone Agent**. This is useful for:
1.  **Simulation**: Connecting to a virtual drone (SITL) running on Mission Planner.
2.  **Telemetry Radio**: Connecting to a real drone via a USB Telemetry Radio.

---

## 1. Prerequisites

### Install Python
1.  Download **Python 3.11** (or newer) from [python.org](https://www.python.org/downloads/).
2.  **IMPORTANT**: During installation, check the box **"Add Python to PATH"**.

### Install Git (Optional)
If you haven't already, install [Git for Windows](https://git-scm.com/download/win) to clone the repository.

---

## 2. Install Dependencies

Open **Command Prompt (cmd)** or **PowerShell** in the `Nidar Project` folder.

Run the following command to install required libraries (`pymavlink`, `requests`, etc.):

```cmd
pip install -r drone_agent/requirements.txt
```

*If you see errors about pip not being recognized, ensure Python was added to your PATH.*

---

## 3. Connecting a Drone (Two Ways)

### Option A: Simulation (SITL)
If you don't have a real drone, use Mission Planner to simulate one.

1.  Open **Mission Planner**.
2.  Click **Simulation** (top bar).
3.  Choose **Multirotor** and click link **Model**.
4.  The simulator will start and open TCP/UDP ports (usually `TCP 5760`).

### Option B: Real Hardware (Telemetry Radio)
1.  Plug your Telemetry Radio (Holybro/SiK) into the laptop USB.
2.  Open **Device Manager** -> **Ports (COM & LPT)**.
3.  Note the COM port number (e.g., `COM3`, `COM4`).

---

## 4. Running the Agent

We have created a simple script to run the agent.

1.  Double-click **`run_agent.bat`**.
2.  Follow the on-screen prompts:

| Option | Description | Connection String Example |
| :--- | :--- | :--- |
| **1. Simulation** | Runs a mock drone (internal test). | N/A |
| **2. WiFi / SITL** | Connects to MP Simulation or WiFi Drone. | `udp:127.0.0.1:14550` or `tcp:127.0.0.1:5760` |
| **3. COM Port** | Connects to USB Telemetry Radio. | `COM3` |

### Example: Connecting to Mission Planner SITL
1.  Run **Mission Planner SITL**.
2.  Run **`run_agent.bat`**.
3.  Select **Option 2**.
4.  Enter Connection: `tcp:127.0.0.1:5760` (Check MP console for the port).

---

## 5. Verification
Once connected:
1.  The console should show: `Agent Loop Started`.
2.  Open **NIDAR Dashboard** (`http://localhost`).
3.  A drone icon should appear on the map.
