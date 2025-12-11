# MAVProxy Integration

The NIDAR GCS is designed to work with ArduPilot. You can use standard MAVProxy to feed data into the GCS.

## Use Case
You have a real drone connected to your laptop (via Telemetry Radio or USB), and you want to forward that connection to the **NIDAR Backend** or **MAV Bridge**.

## Setup command
Run MAVProxy on your system (where the drone is connected):

```bash
mavproxy.py --master /dev/ttyUSB0 --baudrate 57600 --out udp:127.0.0.1:14550
```

- `--master`: Your drone connection (USB/Radio/SITL).
- `--out`: Forwards MAVLink packets to `localhost:14550`.

## Connecting NIDAR
Now run the `mav_bridge/bridge.py` on the same machine:

```bash
python mav_bridge/bridge.py --connect udpin:0.0.0.0:14550
```

The Bridge will now "hear" the drone via MAVProxy and relay mission uploads/telemetry to the Backend.
