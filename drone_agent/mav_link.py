from pymavlink import mavutil
import time
import logging

class DroneController:
    def __init__(self, connection_string):
        logging.info(f"Connecting to drone at {connection_string}...")
        self.master = mavutil.mavlink_connection(connection_string)
        self.master.wait_heartbeat()
        logging.info("Heartbeat received!")

    def get_telemetry(self):
        # Request data if not coming in automatically
        # self.master.mav.request_data_stream_send(...) 
        
        msg = self.master.recv_match(type='GLOBAL_POSITION_INT', blocking=False)
        sys_status = self.master.recv_match(type='SYS_STATUS', blocking=False)
        vfr_hud = self.master.recv_match(type='VFR_HUD', blocking=False)
        
        # Defaults
        lat, lon, alt = 0, 0, 0
        battery = 100
        speed = 0
        heading = 0

        if msg:
            lat = msg.lat / 1e7
            lon = msg.lon / 1e7
            alt = msg.relative_alt / 1000.0 # mm to m
            heading = msg.hdg / 100.0

        if sys_status:
            battery = sys_status.battery_remaining

        if vfr_hud:
            speed = vfr_hud.groundspeed

        return lat, lon, alt, battery, speed, heading

    def upload_mission(self, mission_items):
        if not mission_items:
            return

        logging.info(f"Uploading {len(mission_items)} items...")
        
        # Clear existing
        self.master.mav.mission_clear_all_send(self.master.target_system, self.master.target_component)
        self.master.recv_match(type=['MISSION_ACK'], blocking=True)

        self.master.mav.mission_count_send(self.master.target_system, self.master.target_component, len(mission_items))

        for i, item in enumerate(mission_items):
            msg = self.master.recv_match(type=['MISSION_REQUEST'], blocking=True)
            if not msg:
                logging.error("No mission request received")
                return

            cmd_id = mavutil.mavlink.MAV_CMD_NAV_TAKEOFF if item['command'] == 'TAKEOFF' else mavutil.mavlink.MAV_CMD_NAV_WAYPOINT
            
            # Create MAVLink mission item
            # Seq, Frame, Command, Current, Autocontinue, p1, p2, p3, p4, x, y, z
            self.master.mav.mission_item_send(
                self.master.target_system,
                self.master.target_component,
                int(item['seq']),
                mavutil.mavlink.MAV_FRAME_GLOBAL_RELATIVE_ALT,
                cmd_id,
                0, 1, # current, autocontinue
                0, 0, 0, 0, # params
                float(item['lat']),
                float(item['lon']),
                float(item['alt'])
            )
            logging.info(f"Sent WP {item['seq']}")

        ack = self.master.recv_match(type=['MISSION_ACK'], blocking=True)
        logging.info(f"Mission Upload Result: {ack}")
        
        # Arm & automatic start (Optional, safer to let pilot do it)
        # self.master.arducopter_arm()

