# Deployment & Network Options

## A. Cloud Relay (Recommended for GSM/4G)
Since Drones are on 4G (CGNAT), they cannot accept incoming connections.
**Solution**: Everything connects OUT to a central VPS (Backend).

1. **VPS Setup**:
   - Rent a VPS (AWS/DigitalOcean).
   - Install Docker & Docker Compose.
   - Run `docker-compose up -d`.
   - Ensure Ports `80` (Frontend), `8000` (API), `9000` (TCP Agents) are open.

2. **Laptop Configuration**:
   - Edit `mav_bridge/bridge.py` to point to VPS IP: `--backend http://<VPS_IP>:8000`.

3. **Drone Configuration**:
   - Edit `pi_agents/start_agent.sh` to point to VPS IP: `--server <VPS_IP>`.

## B. Direct Connect (Laptop Host)
If all devices are on the same VPN or LAN.
1. Run Backend on Laptop.
2. Drones connect to Laptop IP.
3. Laptop Bridge connects to localhost.

## C. Tailscale / VPN
1. Install Tailscale on Laptop, VPS, and Pis.
2. Use Tailscale IPs (100.x.x.x) for all configurations.
3. This bypasses CGNAT and Firewall issues securely.

## D. Nginx Reverse Proxy (TLS)
Secure your VPS with SSL (Let's Encrypt).
```nginx
server {
    listen 443 ssl;
    server_name nidar-gcs.com;
    ...
    location / { proxy_pass http://localhost:80; }
    location /wss/ { proxy_pass http://localhost:8000; } # WSS
}
```
