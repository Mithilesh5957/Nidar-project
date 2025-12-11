import requests
import json
import logging

class GCSClient:
    def __init__(self, gcs_url, vehicle_id):
        self.base_url = gcs_url
        self.vehicle_id = vehicle_id
        self.session = requests.Session()
        logging.info(f"GCS Client initialized for {vehicle_id} at {gcs_url}")

    def send_telemetry(self, lat, lon, alt, battery, speed, heading):
        url = f"{self.base_url}/vehicles/{self.vehicle_id}/telemetry"
        data = {
            "lat": lat,
            "lon": lon,
            "alt": alt,
            "battery": battery,
            "speed": speed,
            "heading": heading
        }
        try:
            self.session.post(url, json=data, timeout=1.0)
        except Exception as e:
            logging.error(f"Failed to send telemetry: {e}")

    def fetch_mission(self):
        """
        Returns list of mission items or None if failed/empty
        """
        url = f"{self.base_url}/vehicles/{self.vehicle_id}/mission"
        try:
            resp = self.session.get(url, timeout=2.0)
            if resp.status_code == 200:
                return resp.json()
        except Exception as e:
            logging.error(f"Failed to fetch mission: {e}")
        return None
