# Raspberry Pi 4 (Scout) Setup Guide

**Target Device**: Raspberry Pi 4B (4GB)
**OS**: Raspberry Pi OS 64-bit (Bookworm)
**Role**: Scout Drone Agent (GSM/4G)

---

## 1. Initial System Prep
Open a terminal on your Pi (or SSH in).

1.  **Update System**:
    ```bash
    sudo apt update
    sudo apt full-upgrade -y
    ```

2.  **Install System Dependencies**:
    *Since you are on Bookworm, we need to set up a proper environment.*
    ```bash
    sudo apt install -y python3-venv python3-pip git libatlas-base-dev
    ```
    *(Note: `libatlas-base-dev` is often needed for numpy/pymavlink performance).*

3.  **Set Hostname** (Optional but recommended):
    ```bash
    sudo hostnamectl set-hostname nidar-scout
    ```
    *Reboot if you changed the hostname.*

---

## 2. Network Setup (Tailscale)
Since you are using GSM, we need a VPN to talk to your Laptop.

1.  **Install Tailscale**:
    ```bash
    curl -fsSL https://tailscale.com/install.sh | sh
    ```
2.  **Connect**:
    ```bash
    sudo tailscale up
    ```
    *Scan the QR code or click the link to authenticate. Make sure your Laptop is also on Tailscale.*

---

## 3. Install Drone Agent Software
Raspberry Pi OS Bookworm restricts installing pip packages globally. We will use a **Virtual Environment**.

1.  **Create Directory**:
    ```bash
    mkdir -p ~/nidar/drone_agent
    cd ~/nidar/drone_agent
    ```

2.  **Copy Files**:
    Transfer the files from your `drone_agent` folder on your laptop to this folder on the Pi.
    *   `main.py`
    *   `mav_link.py`
    *   `gcs_client.py`
    *   `requirements.txt`
    
    *(You can use a USB drive, or `scp` if on the same WiFi for setup)*

3.  **Create Virtual Environment**:
    ```bash
    python3 -m venv venv
    ```

4.  **Activate & Install Requirements**:
    ```bash
    source venv/bin/activate
    pip install -r requirements.txt
    ```
    *(If you don't have the file yet, run: `pip install pymavlink requests pyserial`)*

---

## 4. Hardware Connection
1.  Connect your **Pixhawk/Cube** to the Raspberry Pi USB port.
2.  Identify the port (usually `/dev/ttyACM0` or `/dev/ttyUSB0`):
    ```bash
    ls /dev/ttyACM*
    ```

---

## 5. Auto-Start Service (Systemd)
We will create a background service that automatically starts the agent when the Pi boots.

1.  **Create Service File**:
    ```bash
    sudo nano /etc/systemd/system/nidar-scout.service
    ```

2.  **Paste Configuration**:
    **IMPORTANT**: Replace `<LAPTOP_TAILSCALE_IP>` with your Laptop's IP (e.g., `100.x.y.z`).
    
    ```ini
    [Unit]
    Description=Nidar Scout Agent
    After=network-online.target
    Wants=network-online.target

    [Service]
    User=pi
    WorkingDirectory=/home/pi/nidar/drone_agent
    # Point to the python executable INSIDE the virtual environment
    ExecStart=/home/pi/nidar/drone_agent/venv/bin/python main.py --id scout --gcs http://<LAPTOP_TAILSCALE_IP>:8080/api --connect /dev/ttyACM0
    
    Restart=always
    RestartSec=10

    [Install]
    WantedBy=multi-user.target
    ```

3.  **Enable & Start**:
    ```bash
    sudo systemctl daemon-reload
    sudo systemctl enable nidar-scout
    sudo systemctl start nidar-scout
    ```

4.  **Check Status**:
    ```bash
    sudo systemctl status nidar-scout
    ```
    *You should see "Active: active (running)".*

---

## 6. Verification
1.  Go to your Laptop.
2.  Open the NIDAR Dashboard: `http://localhost`.
3.  You should see the **Scout** drone appear on the map!
