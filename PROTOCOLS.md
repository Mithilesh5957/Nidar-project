# System Protocols & API Reference

## 🌐 HTTP REST API (Drone -> Backend)

Drones communicate with the GCS Backend (`http://<GCS_IP>:8080`) using standard HTTP POST requests.

### 1. Send Telemetry
**Endpoint**: `POST /api/vehicles/{id}/telemetry`
**Payload**:
```json
{
  "lat": 20.12345,
  "lon": 78.65432,
  "alt": 50.0,
  "heading": 180,
  "battery": 95,
  "speed": 12.5,
  "status": "FLYING"
}
```

### 2. Upload Detection
**Endpoint**: `POST /api/upload_detection/{vehicle_id}`
**Parameters**: `lat`, `lon`, `confidence` (QueryParams)
**Body**: Multipart File (`file`)

### 3. Mission Management
#### Fetch Mission
**Endpoint**: `POST /api/vehicles/{id}/mission-fetch`
**Response**: JSON Array of mission items.

#### Upload Mission (GCS -> System)
**Endpoint**: `POST /api/vehicles/{id}/mission-upload`
**Payload**: List of Mission Items.

---

## 🔌 WebSocket (Frontend <-> Backend)

The Frontend communicates with the Backend via STOMP over WebSocket for real-time updates.

**Endpoint**: `/ws`

### Subscriptions
- `/topic/telemetry/{id}`: Real-time telemetry updates for a specific drone.
- `/topic/detections`: New object detections.
- `/topic/missions/{id}`: Mission updates.

---

## 🗺️ MAVLink Integration

The **Drone Agent** (`drone_agent/main.py`) acts as the translator between MAVLink (Drone) and REST API (GCS).

1. **MAVLink Listener**: Connects to Flight Controller (Pixhawk/Cube) via Serial/UDP.
2. **State Aggregator**: Compiles `GLOBAL_POSITION_INT`, `BATTERY_STATUS`, etc.
3. **API Client**: Pushes aggregated state to the GCS Backend at 1Hz.
