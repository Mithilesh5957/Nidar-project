package com.nidar.gcs.service;

import com.nidar.gcs.model.Detection;
import com.nidar.gcs.model.MissionItem;
import com.nidar.gcs.repository.MissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class MissionService {

    @Autowired
    private MissionRepository missionRepo;

    @Transactional
    public void generateMissionForDetection(Detection detection, String targetVehicleId) {
        // Clear existing mission for this vehicle
        missionRepo.deleteByVehicleId(targetVehicleId);

        List<MissionItem> mission = new ArrayList<>();

        // 1. Takeoff to safe altitude (e.g. 30m)
        mission.add(createItem(targetVehicleId, 1, "TAKEOFF", 0, 0, 30));

        // 2. Fly to detection location
        mission.add(createItem(targetVehicleId, 2, "WAYPOINT", detection.getLat(), detection.getLon(), 30));

        // 3. Descend for drop (e.g. 10m)
        mission.add(createItem(targetVehicleId, 3, "WAYPOINT", detection.getLat(), detection.getLon(), 10));

        // 4. Drop payload (Servo command placeholder)
        MissionItem drop = createItem(targetVehicleId, 4, "DO_SET_SERVO", 0, 0, 0);
        drop.setP1(9);
        drop.setP2(1100);
        mission.add(drop);

        // 5. Ascend back to safe altitude
        mission.add(createItem(targetVehicleId, 5, "WAYPOINT", detection.getLat(), detection.getLon(), 30));

        // 6. RTL
        mission.add(createItem(targetVehicleId, 6, "RTL", 0, 0, 0));

        missionRepo.saveAll(mission);
    }

    public List<MissionItem> getMission(String vehicleId) {
        List<MissionItem> mission = missionRepo.findByVehicleIdOrderBySeqAsc(vehicleId);
        return mission != null ? mission : List.of();
    }

    @Transactional
    public void setMission(String vehicleId, List<MissionItem> mission) {
        missionRepo.deleteByVehicleId(vehicleId);
        for (MissionItem item : mission) {
            item.setVehicleId(vehicleId);
        }
        missionRepo.saveAll(mission);
    }

    private MissionItem createItem(String vehicleId, int seq, String cmd, double lat, double lon, double alt) {
        MissionItem item = new MissionItem(seq, cmd, lat, lon, alt, 0, 0, 0, 0);
        item.setVehicleId(vehicleId);
        return item;
    }
}
