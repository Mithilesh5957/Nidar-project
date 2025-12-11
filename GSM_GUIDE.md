# GSM / Raspberry Pi Setup Guide

This guide explains how to set up a Raspberry Pi as a Drone Companion Computer using a **GSM Module (4G/LTE)** for unlimited range connection to your NIDAR GCS.

---

## ⚠️ The Problem: CGNAT
Mobile networks (4G/5G) use **CGNAT**, meaning your Pi **does not have a Public IP**.
*   ❌ You CANNOT connect from Laptop -> Pi.
*   ✅ You MUST connect from Pi -> Laptop (or VPS).

## 🛠️ Hardware Requirements
*   Raspberry Pi 4 or Zero 2 W.
*   4G/LTE USB Modem or HAT (e.g., SIM7600).
*   Pixhawk/Cube Flight Controller (connected via USB/Serial).

---

## 🚀 Step 1: Prepare the Raspberry Pi
1.  **Install Raspberry Pi OS** (Lite version recommended).
2.  **Connect to Internet** (WiFi or 4G).
3.  **Install dependencies**:
    ```bash
    sudo apt update
    sudo apt install python3-pip git
    ```

## 🌐 Step 2: Network Setup (Tailscale)
If your GCS is on your **Laptop** (not a public VPS), you **MUST** use Tailscale to link them.

1.  **Install Tailscale** on the Pi:
    ```bash
    curl -fsSL https://tailscale.com/install.sh | sh
    ```
2.  **Authenticate**:
    ```bash
    sudo tailscale up
    ```
    (Scan QR code or visit the link to add Pi to your specific Tailscale network).

3.  **Install Tailscale on Laptop**: Download from [tailscale.com](https://tailscale.com) and login to the *same* account.
4.  **Get Laptop IP**: Note your Laptop's Tailscale IP (starts with `100.x.x.x`).

---

## 🚁 Step 3: Install Drone Agent
1.  **Clone the Repo** (or correct Copy `drone_agent` folder):
    ```bash
    mkdir -p ~/nidar
    # Copy the 'drone_agent' folder from your PC to the Pi via SCP/SFTP
    ```

2.  **Install Python Libs**:
    ```bash
    cd ~/nidar/drone_agent
    pip3 install -r requirements.txt
    ```

---

## ▶️ Step 4: Run the Agent

### Command Line
Run this command on the Pi (via SSH):

```bash
# REPLACE WITH YOUR GCS IP (Tailscale IP or VPS IP)
python3 main.py --gcs http://100.123.45.67:8080/api --connect /dev/ttyACM0 --id scout
```

*   `--gcs`: Address of your Backend.
*   `--connect`: Path to Flight Controller (usually `/dev/ttyACM0` or `/dev/ttyUSB0`).
*   `--id`: Unique name for this drone (`scout`, `delivery`, etc).

### Auto-Start (Systemd Service)
To make it run automatically on boot:

1.  **Create Service File**:
    ```bash
    sudo nano /etc/systemd/system/drone-agent.service
    ```

2.  **Paste Configuration**:
    ```ini
    [Unit]
    Description=Nidar Drone Agent
    After=network-online.target
    Wants=network-online.target

    [Service]
    User=pi
    WorkingDirectory=/home/pi/nidar/drone_agent
    ExecStart=/usr/bin/python3 main.py --gcs http://100.123.45.67:8080/api --connect /dev/ttyACM0 --id scout
    Restart=always
    RestartSec=10

    [Install]
    WantedBy=multi-user.target
    ```

3.  **Enable & Start**:
    ```bash
    sudo systemctl enable drone-agent
    sudo systemctl start drone-agent
    ```

Your drone is now online! Check the **NIDAR Dashboard** to see telemetry.
