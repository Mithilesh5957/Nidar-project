package com.nidar.gcs.service;

import com.nidar.gcs.model.Detection;
import com.nidar.gcs.model.Mission;
import com.nidar.gcs.model.MissionItem;
import com.nidar.gcs.model.Waypoint;
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
     * Legacy method: Set mission items for a vehicle
     */
    public void setMission(String vehicleId, List<MissionItem> missionItems) {
        log.info("Setting mission for vehicle {}: {} items", vehicleId, missionItems.size());
        vehicleMissions.put(vehicleId, missionItems);
    }

    /**
     * Legacy method: Get mission items for a vehicle
     */
    public List<MissionItem> getMission(String vehicleId) {
        return vehicleMissions.getOrDefault(vehicleId, new ArrayList<>());
    }

    /**
     * Legacy method: Generate mission for a detection target
     */
    public void generateMissionForDetection(Detection detection, String vehicleId) {
        log.info("Generating mission for detection {} to vehicle {}", detection.getId(), vehicleId);
        List<MissionItem> mission = new ArrayList<>();

        // Takeoff
        mission.add(new MissionItem(0, "TAKEOFF", 0, 0, 50, 0, 0, 0, 0));

        // Navigate to detection location
        mission.add(new MissionItem(1, "WAYPOINT", detection.getLat(), detection.getLon(), 50, 0, 0, 0, 0));

        // Land
        mission.add(new MissionItem(2, "LAND", detection.getLat(), detection.getLon(), 0, 0, 0, 0, 0));

        setMission(vehicleId, mission);
    }
}
