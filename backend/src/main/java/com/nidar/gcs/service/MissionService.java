package com.nidar.gcs.service;

import com.nidar.gcs.model.Detection;
import com.nidar.gcs.model.MissionItem;
import com.nidar.gcs.repository.MissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import org.springframework.lang.NonNull;

@Service
public class MissionService {

    @Autowired
    private MissionRepository missionRepo;

    @Transactional
    public void generateMissionForDetection(Detection detection, String targetVehicleId) {
        // Clear existing mission for this vehicle
        missionRepo.deleteByVehicleId(targetVehicleId);

        List<MissionItem> mission = new ArrayList<>();

        // 1. Takeoff to safe altitude (e.g. 30m) - MAV_CMD_NAV_TAKEOFF (22)
        mission.add(createItem(targetVehicleId, 1, 22, 0, 0, 30));

        // 2. Fly to detection location - MAV_CMD_NAV_WAYPOINT (16)
        mission.add(createItem(targetVehicleId, 2, 16, detection.getLat(), detection.getLon(), 30));

        // 3. Descend for drop (e.g. 10m)
        mission.add(createItem(targetVehicleId, 3, 16, detection.getLat(), detection.getLon(), 10));

        // 4. Drop payload (Servo command placeholder) - MAV_CMD_DO_SET_SERVO (183)
        MissionItem drop = createItem(targetVehicleId, 4, 183, 0, 0, 0);
        drop.setParam1(9); // Servo Instance
        drop.setParam2(1100); // PWM
        mission.add(drop);

        // 5. Ascend back to safe altitude
        mission.add(createItem(targetVehicleId, 5, 16, detection.getLat(), detection.getLon(), 30));

        // 6. RTL - MAV_CMD_NAV_RETURN_TO_LAUNCH (20)
        mission.add(createItem(targetVehicleId, 6, 20, 0, 0, 0));

        missionRepo.saveAll(mission);
    }

    public List<MissionItem> getMission(String vehicleId) {
        List<MissionItem> mission = missionRepo.findByVehicleIdOrderBySeqAsc(vehicleId);
        return mission != null ? mission : List.of();
    }

    @Transactional
    public void setMission(String vehicleId, @NonNull List<MissionItem> mission) {
        missionRepo.deleteByVehicleId(vehicleId);
        for (MissionItem item : mission) {
            item.setVehicleId(vehicleId);
        }
        missionRepo.saveAll(mission);
    }

    private MissionItem createItem(String vehicleId, int seq, int cmd, double lat, double lon, double alt) {
        MissionItem item = new MissionItem();
        item.setVehicleId(vehicleId);
        item.setSeq(seq);
        item.setCommand(cmd);
        item.setFrame(3); // MAV_FRAME_GLOBAL_RELATIVE_ALT
        item.setCurrent(seq == 0 ? 1 : 0); // Logic can be improved if sequence is 0
        item.setAutocontinue(1);
        item.setX(lat);
        item.setY(lon);
        item.setZ((float)alt);
        return item;
    }
}
