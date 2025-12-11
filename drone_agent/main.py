import time
import argparse
import logging
from gcs_client import GCSClient
from mav_link import DroneController

# Configure Logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

def main():
    parser = argparse.ArgumentParser(description='NIDAR Drone Agent')
    parser.add_argument('--connect', default='udp:127.0.0.1:14550', help='MAVLink connection string')
    parser.add_argument('--id', default='delivery', help='Vehicle ID (scout/delivery)')
    parser.add_argument('--gcs', default='http://localhost:8080/api', help='GCS API URL')
    parser.add_argument('--sim', action='store_true', help='Simulation mode (Virtual Drone)')
    args = parser.parse_args()

    gcs = GCSClient(args.gcs, args.id)
    
    if args.sim:
        logging.info("Starting in SIMULATION mode")
        drone = MockDrone()
    else:
        drone = DroneController(args.connect)

    last_mission_check = 0
    mission_check_interval = 5.0

    logging.info("Agent Loop Started")
    while True:
        try:
            # 1. Telemetry Loop (10Hz approx)
            lat, lon, alt, bat, spd, hdg = drone.get_telemetry()
            
            # 2. Send to GCS (Throttled inside client or here? Let's send every loop for smoothness, 10hz is fine for local)
            # Actually, let's limit to 4Hz to save network
            gcs.send_telemetry(lat, lon, alt, bat, spd, hdg)
            
            # 3. Check for Missions
            if time.time() - last_mission_check > mission_check_interval:
                mission = gcs.fetch_mission()
                # Basic check: In a real agent we'd check if mission ID changed
                # For now, if we get a waiting mission, we upload it
                if mission:
                     logging.info("New mission received from GCS")
                     drone.upload_mission(mission)
                last_mission_check = time.time()

            time.sleep(0.1) # 10Hz loop
            
        except KeyboardInterrupt:
            break
        except Exception as e:
            logging.error(f"Loop error: {e}")
            time.sleep(1)

class MockDrone:
    def __init__(self):
        self.lat = 20.0
        self.lon = 78.0
        self.alt = 0
        self.tick = 0
    
    def get_telemetry(self):
        self.tick += 0.01
        self.lat += 0.00001
        self.lon += 0.00001
        return self.lat, self.lon, 50, 95, 12, 45
    
    def upload_mission(self, items):
        logging.info("SIM: Mission Received")
        for i in items:
            logging.info(f"WP: {i}")

if __name__ == '__main__':
    main()
