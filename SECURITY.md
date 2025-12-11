# Security Considerations

## 🔒 Network Security

The current implementation is designed for rapid deployment and ease of use. For production environments, the following layers should be added:

### 1. Transport Layer Security (TLS/SSL)
Never expose the GCS over plain HTTP in a public environment. Use an **Nginx Reverse Proxy** with Let's Encrypt certificates to ensure all traffic is encrypted (HTTPS/WSS).

### 2. Network Isolation (VPN)
For high-security operations, do not expose ports 80/8080 to the public internet. Instead, use a private VPN mesh network like **Tailscale** or **ZeroTier**.
- Install Tailscale on the Cloud VPS.
- Install Tailscale on all Drone Companions (Raspberry Pis).
- Configure the Drone Agents to report to the VPS's Tailscale IP (e.g., `100.x.y.z`).
- This makes the entire C2 link invisible to the public internet.

## 🔑 Authentication (Roadmap)

Currently, the system uses an open API model suitable for private networks.

**Future Enhancements:**
- **API Keys**: Implement an `x-api-key` header check in a Spring Boot Filter for all `/api/` endpoints.
- **User Login**: Add Spring Security with JWT tokens to restrict Dashboard access to authorized personnel only.
