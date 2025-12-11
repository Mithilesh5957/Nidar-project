# Deployment Guide

This system is designed to be deployed using **Docker Compose** for maximum portability and ease of setup.

## ☁️ Cloud / VPS Deployment (Production)

To deploy the Ground Control System on a remote server (e.g., AWS EC2, DigitalOcean Droplet):

1. **Provision a VPS**
   - Recommended Specs: 2 vCPU, 4GB RAM (Java + MySQL needs some memory).
   - OS: Ubuntu 20.04 / 22.04 LTS.

2. **Install Docker & Docker Compose**
   ```bash
   sudo apt update
   sudo apt install docker.io docker-compose
   ```

3. **Deploy the System**
   Copy the project files to the server and run:
   ```bash
   docker-compose up --build -d
   ```

4. **Network Configuration**
   Ensure the following inbound ports are allowed in your firewall (Security Groups):
   - **TCP 80**: Frontend (HTTP)
   - **TCP 8080**: Backend API (Drone Telemetry & WebSocket)

### 🌍 Domain & SSL (Nginx Proxy)

For a production setup, it is highly recommended to put an Nginx reverse proxy in front of the services to handle SSL (HTTPS).

**Example Nginx Config:**
```nginx
server {
    listen 443 ssl;
    server_name groundcontrol.yourdomain.com;

    # Frontend
    location / {
        proxy_pass http://localhost:80;
    }

    # Backend API & WebSocket
    location /api {
        proxy_pass http://localhost:8080;
    }
    
    location /ws {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "Upgrade";
    }
}
```

---

## 💻 Local Deployment (Testing)

For local testing or LAN usage:

1. **Run Startup Script**
   - Windows: `START_SYSTEM.bat`
   - Linux/Mac: `docker-compose up --build`

2. **Access**
   - Dashboard: `http://localhost`
   - API: `http://localhost:8080`

## 📡 Drone Network Topology (GSM / 4G)

**IMPORTANT: Drones on 4G/GSM cannot accept incoming connections due to Carrier Grade NAT (CGNAT).**

You have two options to connect GSM Drones to your GCS:

### Option A: Public VPS (Recommended for Production)
Deploy the GCS Backend to a **Public VPS** (AWS/DigitalOcean).  
The Drone Agent can "call home" to the flexible Public IP of the VPS.

### Option B: Tailscale VPN (Recommended for Testing)
If you want to run the GCS on your **Laptop** and connect a **GSM Drone** to it, you MUST use a VPN like **Tailscale** to create a private network mesh.

👉 **[See detailed GSM / Raspberry Pi Guide](GSM_GUIDE.md)** for step-by-step setup.

### Connection Flow
1. **Drones -> GCS**: Drones initiate HTTP/TCP connections to the GCS Server IP.
2. **Reverse Access**: The GCS relies on the open connection (or polling) to send commands back, or simply waits for the next heartbeat.
