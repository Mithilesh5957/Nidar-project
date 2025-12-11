# MAVProxy Integration

The NIDAR GCS is designed to work with ArduPilot. You can use standard MAVProxy to feed data into the GCS via the Drone Agent.

## Use Case
You have a real drone connected to your laptop (via Telemetry Radio or USB), and you want to forward that connection to the **Drone Agent**.

## Setup command
Run MAVProxy on your system (where the drone is connected):

```bash
mavproxy.py --master /dev/ttyUSB0 --baudrate 57600 --out udp:127.0.0.1:14550
```

- `--master`: Your drone connection (USB/Radio/SITL).
- `--out`: Forwards MAVLink packets to `localhost:14550` (Standard MAVLink UDP port).

## Connecting NIDAR Agent
Now run the **Drone Agent** using the script:

1. Double-click **`run_agent.bat`**.
2. Select **Option 2 (WiFi / SITL / UDP)**.
3. Enter Connection String: `udpin:0.0.0.0:14550` (or leave default if it matches).

The Agent will now "hear" the drone via MAVProxy and relay mission uploads/telemetry to the **NIDAR Backend** (`http://localhost:8080`).
