# Test Plan (SITL & Simulation)

## 1. Setup Simulation
1. **Laptop**: Install ArduPilot SITL.
   ```bash
   sim_vehicle.py -v ArduCopter --console --map -I0 --sysid 1
   ```
   *This starts a virtual drone listening on TCP 5760 and outputting MAVLink to 14550.*

## 2. Start Backend
1. **Terminal 1**:
   ```bash
   cd backend
   uvicorn main:app --reload --port 8000
   ```
   *Or use Docker.*

## 3. Run Connectors
1. **Bridge (Laptop)**:
   ```bash
   python mav_bridge/bridge.py --connect udpin:0.0.0.0:14550
   ```
   *Verify it sees the SITL heartbeat.*

2. **Delivery Agent (Simulating Pi on Laptop)**:
   ```bash
   python pi_agents/delivery_agent.py --connect tcp:127.0.0.1:5763 --server localhost
   ```
   *Note: SITL opens extra ports. Use 5763 or similar for the agent to connect.*

## 4. Execute Test Mission
1. **Mission Planner**:
   - Connect to `udp:127.0.0.1:14550`.
   - Create a generic Waypoint mission.
   - Click **Write**.
2. **Verify**:
   - **Bridge Log**: "RX MISSION_ITEM... Uploading to Backend".
   - **Backend Log**: "Received Mission... Sent to delivery agent".
   - **Agent Log**: "Uploading to Flight Controller... ACK Received".
   - **SITL Console**: "Flight plan received".

## 5. Test Scout Detection
1. **Run Scout Agent**:
   ```bash
   python pi_agents/scout_agent.py --server localhost
   ```
2. **Wait**: It will auto-upload a fake image every ~10s.
3. **Frontend**: Open `http://localhost`. Go to **Analysis/Intelligence**.
4. **Click Approve**: Verify "Approved" status update via WebSocket.
