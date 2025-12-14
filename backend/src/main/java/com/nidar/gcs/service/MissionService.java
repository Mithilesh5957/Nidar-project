package com.nidar.gcs.service;

import com.nidar.gcs.model.Detection;
import com.nidar.gcs.model.Mission;
import com.nidar.gcs.model.MissionItem;
import com.nidar.gcs.model.Waypoint;
import com.nidar.gcs.repository.MissionItemRepository;
import com.nidar.gcs.repository.MissionRepository;
import com.nidar.gcs.repository.WaypointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.lang.NonNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class MissionService {

    private final MissionRepository missionRepository;
    private final WaypointRepository waypointRepository;
    private final MissionItemRepository missionItemRepository;
    private final MAVProxyService mavProxyService;

    @Transactional
    public Mission createMission(Mission mission) {
        mission.setCreatedAt(LocalDateTime.now());
        mission.setStatus("CREATED");

        Mission savedMission = missionRepository.save(mission);

        // Set mission reference for waypoints
        if (mission.getWaypoints() != null) {
            for (Waypoint waypoint : mission.getWaypoints()) {
                waypoint.setMission(savedMission);
            }
            waypointRepository.saveAll(java.util.Objects.requireNonNull(mission.getWaypoints()));
        }

        return savedMission;
    }

    public List<Mission> getAllMissions() {
        return missionRepository.findAll();
    }

    public Mission getMissionById(@NonNull Long id) {
        return missionRepository.findById(id).orElse(null);
    }

    @Transactional
    public Mission deployMission(@NonNull Long missionId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new RuntimeException("Mission not found"));

        if (!mavProxyService.isConnected()) {
            throw new RuntimeException("Not connected to drone");
        }

        // Upload complete mission including waypoints, geofence, and rally points
        boolean success = mavProxyService.uploadCompleteMission(mission);

        if (success) {
            mission.setStatus("DEPLOYED");
            mission.setDeployedAt(LocalDateTime.now());
            return missionRepository.save(mission);
        } else {
            throw new RuntimeException("Failed to deploy mission to Mission Planner/QGC");
        }
    }

    @Transactional
    public Mission parseMissionFile(MultipartFile file) throws Exception {
        log.info("Parsing mission file: {}", file.getOriginalFilename());

        Mission mission = new Mission();
        mission.setName(file.getOriginalFilename());
        mission.setDescription("Imported from file");

        List<Waypoint> waypoints = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);

            // Parse waypoints from XML
            NodeList waypointNodes = document.getElementsByTagName("waypoint");

            for (int i = 0; i < waypointNodes.getLength(); i++) {
                Element waypointElement = (Element) waypointNodes.item(i);

                Waypoint waypoint = new Waypoint();
                waypoint.setSequence(i);
                waypoint.setLatitude(Double.parseDouble(getElementValue(waypointElement, "lat")));
                waypoint.setLongitude(Double.parseDouble(getElementValue(waypointElement, "lon")));
                waypoint.setAltitude(Double.parseDouble(getElementValue(waypointElement, "alt")));
                waypoint.setCommand(getElementValue(waypointElement, "command"));

                waypoints.add(waypoint);
            }
        }

        mission.setWaypoints(waypoints);
        log.info("Parsed {} waypoints from mission file", waypoints.size());

        return createMission(mission);
    }

    private String getElementValue(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return "";
    }

    // ============ Legacy compatibility methods for VehicleController and
    // DetectionController ============

    private final Map<String, List<MissionItem>> vehicleMissions = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Legacy method: Get mission items for a vehicle (from database)
     */
    public List<MissionItem> getMission(String vehicleId) {
        List<MissionItem> mission = missionItemRepository.findByVehicleIdOrderBySeqAsc(vehicleId);
        return mission != null ? mission : List.of();
    }

    /**
     * Legacy method: Set mission items for a vehicle (saves to database)
     */
    @Transactional
    public void setMission(String vehicleId, @NonNull List<MissionItem> mission) {
        missionItemRepository.deleteByVehicleId(vehicleId);
        for (MissionItem item : mission) {
            item.setVehicleId(vehicleId);
        }
        missionItemRepository.saveAll(mission);
    }

    /**
     * Generate mission for a detection target with full MAVLink commands
     * Includes: Takeoff -> Navigate -> Descend -> Drop Payload -> Ascend -> RTL
     */
    public void generateMissionForDetection(Detection detection, String targetVehicleId) {
        log.info("Generating mission for detection {} to vehicle {}", detection.getId(), targetVehicleId);
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

        missionItemRepository.saveAll(mission);
    }

    /**
     * Helper method to create a MissionItem with MAVLink-compatible fields
     */
    private MissionItem createItem(String vehicleId, int seq, int cmd, double lat, double lon, double alt) {
        MissionItem item = new MissionItem();
        item.setVehicleId(vehicleId);
        item.setSeq(seq);
        item.setCommand(cmd);
        item.setFrame(3); // MAV_FRAME_GLOBAL_RELATIVE_ALT
        item.setCurrent(seq == 1 ? 1 : 0); // First mission item is current
        item.setAutocontinue(1);
        item.setX(lat);
        item.setY(lon);
        item.setZ((float) alt);
        return item;
    }
}
