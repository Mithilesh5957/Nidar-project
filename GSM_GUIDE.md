# GSM Connectivity Guide

Since the Pi uses GSM, it is behind a carrier firewall (CGNAT) and cannot receive incoming connections. The GCS cannot "connect to" the Pi.

## Requirements
The **Laptop/GCS** must be reachable from the internet.

## Solution: Tailscale (Recommended)
The easiest way to bridge the Pi (GSM) and Laptop (WiFi) is a mesh VPN.

1.  **Install Tailscale** on both Laptop and Pi.
2.  **Connect** both to the same Tailnet.
3.  **Use Tailscale IP**: On the Pi, point the agent to the Laptop's Tailscale IP.
    ```bash
    python drone_agent/main.py --gcs http://100.x.y.z:8080/api --connect /dev/ttyUSB0
    ```

## Code Update
Ensure `drone_agent` handles connection drops gracefully (GSM is flaky).
