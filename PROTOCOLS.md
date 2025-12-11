# System Protocols & Formats

## 1. MAVLink Bridge -> Backend
**Endpoint**: `POST /internal/mission/from_laptop`
**Format**: JSON Array of Mission Items
```json
[
  {
    "seq": 0,
    "command": 16,
    "param1": 0, "param2": 0, "param3": 0, "param4": 0,
    "x": 20.5, "y": 78.5, "z": 100,
    "frame": 3
  },
  ...
]
```

## 2. Pi Agents -> Backend (TCP)
**Handshake**:
```json
{"type": "identify", "vehicle_id": "scout", "key": "scout_secret"}
```

**Telemetry (Agent -> Backend)**:
```json
{
  "type": "telemetry",
  "payload": {
    "id": "scout",
    "lat": 20.123,
    "lon": 78.123,
    "alt": 50.5,
    "battery": 95,
    "mode": "GUIDED",
    "speed": 12.0
  }
}
```

**Mission Upload (Backend -> Agent)**:
```json
{
  "type": "upload_mission",
  "mission_items": [ ... (same as bridge format) ... ]
}
```

**ACK (Agent -> Backend)**:
```json
{
  "type": "ack",
  "status": "Mission Uploaded"
}
```

## 3. Scout Detection -> Backend (HTTP)
**Endpoint**: `POST /api/detections/{vehicle_id}`
**Headers**: `x-api-key: <OPTIONAL_IF_IMPLEMENTED>`
**Multipart Form Data**:
- `file`: (Binary Image)
- `confidence`: "0.98"
- `lat`: "20.123"
- `lon`: "78.123"
