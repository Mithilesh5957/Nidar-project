package com.nidar.gcs.service;

import com.nidar.gcs.model.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import org.springframework.lang.NonNull;

@Service
@Slf4j
public class MAVProxyService {

    @Value("${mavproxy.host:localhost}")
    private String mavproxyHost;

    @Value("${mavproxy.port:14550}")
    private int mavproxyPort;

    @Value("${mavlink.simulation.enabled:true}")
    private boolean simulationEnabled;

    @Value("${mavlink.serial.enabled:false}")
    private boolean serialEnabled;

    private final SimpMessagingTemplate messagingTemplate;
    private final TelemetryService telemetryService;
    private final MAVLinkMessageService mavLinkMessageService;
    private final MissionExecutionService missionExecutionService;
    private final MAVLinkSerialService mavLinkSerialService;

    private DatagramSocket udpSocket;
    private InetAddress mavproxyAddress;
    private boolean connected = false;
    private Random random = new Random();

    public MAVProxyService(SimpMessagingTemplate messagingTemplate,
            TelemetryService telemetryService,
            MAVLinkMessageService mavLinkMessageService,
            MissionExecutionService missionExecutionService,
            MAVLinkSerialService mavLinkSerialService) {
        this.messagingTemplate = messagingTemplate;
        this.telemetryService = telemetryService;
        this.mavLinkMessageService = mavLinkMessageService;
        this.missionExecutionService = missionExecutionService;
        this.mavLinkSerialService = mavLinkSerialService;
    }

    @PostConstruct
    public void init() {
        if (serialEnabled) {
            log.info("Real drone mode (serial enabled) - MAVLink data will come from flight controller on COM port");
            connect(); // Connect for sending data to QGC
        } else if (simulationEnabled) {
            log.info("MAVLink simulation enabled - auto-connecting to QGC...");
            connect();
        } else {
            log.info(
                    "MAVLink simulation disabled - virtual drone will not be started. Connect your real drone to QGC.");
        }
    }

    public boolean connect() {
        try {
            log.info("Attempting to connect to MAVProxy at {}:{}", mavproxyHost, mavproxyPort);

            // Initialize UDP socket for communication
            udpSocket = new DatagramSocket();
            mavproxyAddress = InetAddress.getByName(mavproxyHost);

            connected = true;
            log.info("Successfully connected to MAVProxy");

            // Send basic firmware parameters to satisfy QGC
            sendInitialParameters();

            return true;
        } catch (Exception e) {
            log.error("Failed to connect to MAVProxy", e);
            connected = false;
            return false;
        }
    }

    /**
     * Send initial firmware parameters to QGC to satisfy parameter requirements
     */
    private void sendInitialParameters() {
        if (udpSocket == null || mavproxyAddress == null) {
            return;
        }

        try {
            // Basic ArduPilot parameters that QGC expects
            String[][] params = {
                    { "FS_OPTIONS", "0" }, // Failsafe options
                    { "H12_OOS_ENABLE", "0" }, // Heli out-of-sync enable
                    { "H12_OOS_THRESHOLD", "0" }, // Heli out-of-sync threshold
                    { "SYSID_THISMAV", "1" }, // MAVLink system ID
                    { "SYSID_MYGCS", "255" }, // Ground station system ID
                    { "FLTMODE1", "0" }, // Flight mode 1
                    { "FLTMODE2", "2" }, // Flight mode 2
                    { "FLTMODE3", "3" }, // Flight mode 3
                    { "RTL_ALT", "100" }, // RTL altitude (cm)
                    { "WP_YAW_BEHAVIOR", "0" }, // Yaw behavior at waypoints
            };

            int paramCount = params.length;
            for (int i = 0; i < params.length; i++) {
                String paramName = params[i][0];
                float paramValue = Float.parseFloat(params[i][1]);
                mavLinkMessageService.sendParamValue(udpSocket, mavproxyAddress, mavproxyPort,
                        paramName, paramValue, i, paramCount);
                Thread.sleep(10); // Small delay between parameters
            }

            log.info("Sent {} initial parameters to QGC", paramCount);
        } catch (Exception e) {
            log.error("Failed to send initial parameters", e);
        }
    }

    public void disconnect() {
        try {
            if (udpSocket != null && !udpSocket.isClosed()) {
                udpSocket.close();
            }
            connected = false;
            log.info("Disconnected from MAVProxy");
        } catch (Exception e) {
            log.error("Error disconnecting from MAVProxy", e);
        }
    }

    public boolean isConnected() {
        if (serialEnabled) {
            return mavLinkSerialService.isConnected();
        }
        return connected;
    }

    /**
     * Update telemetry data every second
     * Sends active MAVLink data to QGC (Heartbeat + Position) and WebSocket updates
     * to Frontend.
     * 
     * When simulation=false: Processes mission execution but doesn't send virtual
     * drone telemetry
     * When simulation=true: Sends virtual drone telemetry to QGC
     */
    @Scheduled(fixedRate = 1000) // Update every second
    public void updateTelemetry() {
        if (!connected) {
            return;
        }

        // Update mission execution (if active) - works for both real and virtual
        missionExecutionService.update();

        // Generate telemetry based on mission execution or default position
        Telemetry telemetry = generateTelemetry();
        telemetryService.saveTelemetry(telemetry);

        // Send telemetry via WebSocket to frontend
        messagingTemplate.convertAndSend("/topic/telemetry", telemetry);

        // Only send MAVLink updates if simulation is enabled (virtual drone)
        // Real drone sends its own MAVLink via hardware/QGC
        if (simulationEnabled) {
            // Send MAVLink updates to QGC (Mission Planner)
            try {
                if (udpSocket != null && !udpSocket.isClosed()) {
                    // 1. Send Heartbeat (Required for QGC to see connection)
                    mavLinkMessageService.sendHeartbeat(udpSocket, mavproxyAddress, mavproxyPort);

                    // 2. Send Global Position (Required for map tracking)
                    mavLinkMessageService.sendGlobalPositionInt(udpSocket, mavproxyAddress, mavproxyPort, telemetry);
                }
            } catch (Exception e) {
                log.error("Failed to send MAVLink updates to QGC", e);
                // Don't disconnect, just log error and retry next loop
            }
        }
    }

    @NonNull
    private Telemetry generateTelemetry() {
        Telemetry telemetry = new Telemetry();

        // Use mission execution data if mission is active
        if (missionExecutionService.isMissionActive() || missionExecutionService.isArmed()) {
            telemetry.setLatitude(missionExecutionService.getLatitude());
            telemetry.setLongitude(missionExecutionService.getLongitude());
            telemetry.setAltitude(missionExecutionService.getAltitude());
            telemetry.setSpeed(missionExecutionService.getSpeed());
            telemetry.setBattery(missionExecutionService.getBattery());
            telemetry.setHeading((int) missionExecutionService.getHeading());
            telemetry.setFlightMode(missionExecutionService.getFlightMode());
            telemetry.setArmed(missionExecutionService.isArmed());
        } else {
            // Drone is idle - use stationary position with random small variations for
            // realism
            double baseLatitude = 40.7128;
            double baseLongitude = -74.0060;

            telemetry.setLatitude(baseLatitude + (random.nextDouble() - 0.5) * 0.0001); // ~10m variation
            telemetry.setLongitude(baseLongitude + (random.nextDouble() - 0.5) * 0.0001);
            telemetry.setAltitude(0.0); // On ground
            telemetry.setSpeed(0.0);
            telemetry.setBattery(100.0);
            telemetry.setHeading(0);
            telemetry.setFlightMode("STABILIZE");
            telemetry.setArmed(false);
        }

        telemetry.setSatellites(12 + random.nextInt(3)); // GPS quality
        telemetry.setTimestamp(LocalDateTime.now());
        return telemetry;
    }

    public boolean uploadMission(List<Waypoint> waypoints) {
        if (!connected) {
            log.error("Cannot upload mission: Not connected to MAVProxy");
            return false;
        }

        try {
            log.info("Uploading mission with {} waypoints to Mission Planner/QGC", waypoints.size());

            // Step 1: Send mission count
            if (!mavLinkMessageService.sendMissionCount(udpSocket, mavproxyAddress, mavproxyPort, waypoints.size())) {
                throw new RuntimeException("Failed to send mission count");
            }

            // Small delay between messages
            Thread.sleep(100);

            // Step 2: Send each waypoint
            for (int i = 0; i < waypoints.size(); i++) {
                Waypoint wp = waypoints.get(i);
                log.info("Sending waypoint {}: lat={}, lon={}, alt={}",
                        i, wp.getLatitude(), wp.getLongitude(), wp.getAltitude());

                if (!mavLinkMessageService.sendMissionItem(udpSocket, mavproxyAddress, mavproxyPort, wp, i)) {
                    throw new RuntimeException("Failed to send waypoint " + i);
                }

                // Small delay between waypoints
                Thread.sleep(50);
            }

            log.info("Mission uploaded successfully to Mission Planner/QGC");
            return true;
        } catch (Exception e) {
            log.error("Failed to upload mission", e);
            return false;
        }
    }

    /**
     * Upload complete mission including waypoints, geofence, and rally points
     */
    public boolean uploadCompleteMission(Mission mission) {
        if (!connected) {
            log.error("Cannot upload mission: Not connected to MAVProxy");
            return false;
        }

        try {
            log.info("Uploading complete mission '{}' to Mission Planner/QGC", mission.getName());

            // Upload waypoints
            if (mission.getWaypoints() != null && !mission.getWaypoints().isEmpty()) {
                if (!uploadMission(mission.getWaypoints())) {
                    return false;
                }
            }

            // Upload geofence points
            if (mission.getGeofenceEnabled() && mission.getGeofencePoints() != null &&
                    !mission.getGeofencePoints().isEmpty()) {
                uploadGeofence(mission.getGeofencePoints());
            }

            // Upload rally points
            if (mission.getRallyPoints() != null && !mission.getRallyPoints().isEmpty()) {
                uploadRallyPoints(mission.getRallyPoints());
            }

            log.info("Complete mission uploaded to Mission Planner/QGC");
            return true;
        } catch (Exception e) {
            log.error("Failed to upload complete mission", e);
            return false;
        }
    }

    /**
     * Upload geofence points to Mission Planner/QGC
     */
    public boolean uploadGeofence(List<GeofencePoint> points) {
        try {
            log.info("Uploading {} geofence points to Mission Planner/QGC", points.size());

            for (int i = 0; i < points.size(); i++) {
                GeofencePoint point = points.get(i);
                mavLinkMessageService.sendGeofencePoint(udpSocket, mavproxyAddress, mavproxyPort,
                        point, i, points.size());
                Thread.sleep(50);
            }

            log.info("Geofence uploaded successfully");
            return true;
        } catch (Exception e) {
            log.error("Failed to upload geofence", e);
            return false;
        }
    }

    /**
     * Upload rally points to Mission Planner/QGC
     */
    public boolean uploadRallyPoints(List<RallyPoint> points) {
        try {
            log.info("Uploading {} rally points to Mission Planner/QGC", points.size());

            for (int i = 0; i < points.size(); i++) {
                RallyPoint point = points.get(i);
                mavLinkMessageService.sendRallyPoint(udpSocket, mavproxyAddress, mavproxyPort,
                        point, i, points.size());
                Thread.sleep(50);
            }

            log.info("Rally points uploaded successfully");
            return true;
        } catch (Exception e) {
            log.error("Failed to upload rally points", e);
            return false;
        }
    }

    /**
     * Upload vehicle parameters to Mission Planner/QGC
     */
    public boolean uploadParameters(List<VehicleParameter> parameters) {
        if (!connected) {
            log.error("Cannot upload parameters: Not connected to MAVProxy");
            return false;
        }

        try {
            log.info("Uploading {} parameters to Mission Planner/QGC", parameters.size());

            for (VehicleParameter param : parameters) {
                float value = Float.parseFloat(param.getParameterValue());
                mavLinkMessageService.sendParameter(udpSocket, mavproxyAddress, mavproxyPort,
                        param.getParameterName(), value);
                Thread.sleep(50);
            }

            log.info("Parameters uploaded successfully to Mission Planner/QGC");
            return true;
        } catch (Exception e) {
            log.error("Failed to upload parameters", e);
            return false;
        }
    }

    public boolean sendCommand(String command) {
        if (!connected) {
            log.error("Cannot send command: Not connected to MAVProxy");
            return false;
        }

        try {
            // In a real implementation, this would send commands to MAVProxy
            log.info("Sending command: {}", command);
            return true;
        } catch (Exception e) {
            log.error("Failed to send command", e);
            return false;
        }
    }
}
