package com.nidar.gcs.service;

import com.nidar.gcs.model.Detection;
import com.nidar.gcs.model.MissionItem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MissionService {

    // Store missions per vehicle
    private final Map<String, List<MissionItem>> activeMissions = new ConcurrentHashMap<>();

    public void generateMissionForDetection(Detection detection, String targetVehicleId) {
        List<MissionItem> mission = new ArrayList<>();

        // 1. Takeoff to safe altitude (e.g. 30m)
        mission.add(new MissionItem(1, "TAKEOFF", 0, 0, 30, 0, 0, 0, 0));

        // 2. Fly to detection location
        mission.add(new MissionItem(2, "WAYPOINT", detection.getLat(), detection.getLon(), 30, 0, 0, 0, 0));

        // 3. Descend for drop (e.g. 10m)
        mission.add(new MissionItem(3, "WAYPOINT", detection.getLat(), detection.getLon(), 10, 0, 0, 0, 0));

        // 4. Drop payload (Servo command placeholder)
        mission.add(new MissionItem(4, "DO_SET_SERVO", 0, 0, 0, 9, 1100, 0, 0)); // Servo 9 to PWM 1100

        // 5. Ascend back to safe altitude
        mission.add(new MissionItem(5, "WAYPOINT", detection.getLat(), detection.getLon(), 30, 0, 0, 0, 0));

        // 6. RTL
        mission.add(new MissionItem(6, "RTL", 0, 0, 0, 0, 0, 0, 0));

        activeMissions.put(targetVehicleId, mission);
    }

    public List<MissionItem> getMission(String vehicleId) {
        return activeMissions.getOrDefault(vehicleId, new ArrayList<>());
    }

    public void setMission(String vehicleId, List<MissionItem> mission) {
        activeMissions.put(vehicleId, mission);
    }
}
