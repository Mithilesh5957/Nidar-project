package com.nidar.gcs.service;

import com.nidar.gcs.model.MissionItem;
import com.nidar.gcs.model.Vehicle;
import io.dronefleet.mavlink.MavlinkConnection;
import io.dronefleet.mavlink.MavlinkMessage;
import io.dronefleet.mavlink.common.GlobalPositionInt;
import io.dronefleet.mavlink.minimal.Heartbeat;
import io.dronefleet.mavlink.common.SysStatus;
import io.dronefleet.mavlink.minimal.MavState;
import io.dronefleet.mavlink.util.EnumValue;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import io.dronefleet.mavlink.common.CommandLong;
import io.dronefleet.mavlink.common.MavCmd;
import io.dronefleet.mavlink.common.MissionAck;
import io.dronefleet.mavlink.common.MissionCount;
import io.dronefleet.mavlink.common.MissionItemInt;
import io.dronefleet.mavlink.common.MissionRequest;
import io.dronefleet.mavlink.common.MissionRequestInt;
import io.dronefleet.mavlink.common.MavMissionType;
import io.dronefleet.mavlink.common.MavMissionResult;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MavlinkService {

    private static final int UDP_PORT = 14552;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private DatagramSocket socket;
    private ExecutorService executor;
    private boolean running = false;
    
    // Target address (Mission Planner or Drone)
    private InetAddress targetAddress;
    private int targetPort;
    private boolean targetIdentified = false;

    private int gcsSystemId = 255;
    private int gcsComponentId = 190; // Mission Planner
    
    // Mission Upload State
    // Map vehicleId -> List of pending items
    private Map<String, List<MissionItem>> pendingMissions = new ConcurrentHashMap<>();

    @PostConstruct
    public void start() {
        try {
            socket = new DatagramSocket(UDP_PORT);
            executor = Executors.newSingleThreadExecutor();
            running = true;
            executor.submit(this::listen);
            System.out.println("Mavlink UDP listener started on port " + UDP_PORT);
        } catch (Exception e) {
            System.err.println("Failed to start Mavlink listener: " + e.getMessage());
        }
    }

    private void listen() {
        byte[] buffer = new byte[4096];
        
        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                
                // Capture target address from incoming traffic
                if (!targetIdentified || !packet.getAddress().equals(targetAddress) || packet.getPort() != targetPort) {
                    targetAddress = packet.getAddress();
                    targetPort = packet.getPort();
                    targetIdentified = true;
                }
                
                try (var is = new java.io.ByteArrayInputStream(packet.getData(), 0, packet.getLength())) {
                     // Pass null for output stream as we are only reading
                     MavlinkConnection connection = MavlinkConnection.create(is, null);
                     MavlinkMessage<?> message;
                     while ((message = connection.next()) != null) {
                         processMessage(message, packet.getAddress().getHostAddress());
                     }
                } catch (Exception e) {
                    // Packet might contain garbage or incomplete data, ignore.
                }

            } catch (IOException e) {
                 if (running) System.err.println("Mavlink receive error: " + e.getMessage());
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void processMessage(MavlinkMessage<?> message, String sourceIp) {
        int systemId = message.getOriginSystemId();
        String vehicleId = mapSystemIdToVehicleId(systemId);
        
        if (vehicleId == null) return; // Unknown vehicle

        Object payload = message.getPayload();

        if (payload instanceof GlobalPositionInt) {
            GlobalPositionInt pos = (GlobalPositionInt) payload;
            double lat = pos.lat() / 1e7;
            double lon = pos.lon() / 1e7;
            double alt = pos.relativeAlt() / 1000.0;
            double heading = pos.hdg() / 100.0;
            
            Vehicle v = vehicleService.getVehicle(vehicleId);
            double battery = (v != null) ? v.getBattery() : 100;
            String status = (v != null) ? v.getStatus() : "UNKNOWN";

            vehicleService.updateTelemetry(vehicleId, lat, lon, alt, heading, battery, status);
            broadcastUpdate(vehicleId);
        } else if (payload instanceof SysStatus) {
            SysStatus sys = (SysStatus) payload;
            double battery = sys.batteryRemaining();
            
            Vehicle v = vehicleService.getVehicle(vehicleId);
            if (v != null) {
                vehicleService.updateTelemetry(vehicleId, v.getLat(), v.getLon(), v.getAlt(), v.getHeading(), battery, v.getStatus());
                broadcastUpdate(vehicleId);
            }
        } else if (payload instanceof Heartbeat) {
            Heartbeat hb = (Heartbeat) payload;
            String status = mapMavState(hb.systemStatus().entry());
            
            Vehicle v = vehicleService.getVehicle(vehicleId);
            if (v != null) {
                 vehicleService.updateTelemetry(vehicleId, v.getLat(), v.getLon(), v.getAlt(), v.getHeading(), v.getBattery(), status);
                 broadcastUpdate(vehicleId);
            }
        } else if (payload instanceof MissionRequestInt) {
            handleMissionRequest(vehicleId, ((MissionRequestInt) payload).seq());
        } else if (payload instanceof MissionRequest) {
            handleMissionRequest(vehicleId, ((MissionRequest) payload).seq());
        } else if (payload instanceof MissionAck) {
            MissionAck ack = (MissionAck) payload;
            if (ack.type().entry() == MavMissionResult.MAV_MISSION_ACCEPTED) {
                System.out.println("Mission upload accepted by " + vehicleId);
                pendingMissions.remove(vehicleId);
            } else {
                System.err.println("Mission upload failed for " + vehicleId + ": " + ack.type());
            }
        }
    }
    
    private void handleMissionRequest(String vehicleId, int seq) {
        List<MissionItem> mission = pendingMissions.get(vehicleId);
        if (mission == null || seq >= mission.size()) return;
        
        MissionItem item = mission.get(seq);
        sendMissionItem(vehicleId, seq, item);
    }

    private void broadcastUpdate(String vehicleId) {
        Vehicle v = vehicleService.getVehicle(vehicleId);
        if (v != null) {
            messagingTemplate.convertAndSend("/topic/telemetry/" + vehicleId, v);
        }
    }

    // --- Sending Commands ---

    public void armVehicle(String vehicleId) {
        // Target System 1 (Autopilot), Component 0 (All/Broadcast)
        sendCommand(1, 0, MavCmd.MAV_CMD_COMPONENT_ARM_DISARM, 1, 0, 0, 0, 0, 0, 0);
    }

    public void disarmVehicle(String vehicleId) {
        sendCommand(1, 0, MavCmd.MAV_CMD_COMPONENT_ARM_DISARM, 0, 0, 0, 0, 0, 0, 0);
    }
    
    public void returnToLaunch(String vehicleId) {
         sendCommand(1, 0, MavCmd.MAV_CMD_NAV_RETURN_TO_LAUNCH, 0, 0, 0, 0, 0, 0, 0);
    }

    public void takeoff(String vehicleId, float altitude) {
         sendCommand(1, 0, MavCmd.MAV_CMD_NAV_TAKEOFF, 0, 0, 0, 0, 0, 0, altitude);
    }
    
    public void setMode(String vehicleId, String mode) {
        // Mapping basic modes for ArduPilot Copter. 
        // 0=Stabilize, 4=Guided, 9=Land, 6=RTL, 3=Auto. 
        // Custom Mode usually used in SET_MODE.
        // For standard MAV_CMD_DO_SET_MODE: param1=Mode(base), param2=Custom Mode.
        
        float baseMode = 1; // MAV_MODE_FLAG_CUSTOM_MODE_ENABLED
        float customMode = 0;
        
        if ("GUIDED".equalsIgnoreCase(mode)) customMode = 4;
        else if ("AUTO".equalsIgnoreCase(mode)) customMode = 3;
        else if ("RTL".equalsIgnoreCase(mode)) customMode = 6;
        else if ("LAND".equalsIgnoreCase(mode)) customMode = 9;
        
        sendCommand(1, 1, MavCmd.MAV_CMD_DO_SET_MODE, baseMode, customMode, 0, 0, 0, 0, 0);
    }
    
    public void reposition(String vehicleId, double lat, double lon, float alt) {
        // MAV_CMD_DO_REPOSITION
        sendCommand(1, 1, MavCmd.MAV_CMD_DO_REPOSITION, -1, 0, 0, 0, (float)(lat * 1E7), (float)(lon * 1E7), alt);
    }
    
    public void requestDataStream(String vehicleId) {
        // Request detailed stream. MAV_CMD_SET_MESSAGE_INTERVAL is preferred for individual messages.
        // But for general stream, we can try legacy REQUEST_DATA_STREAM if supported, or just set interval for specific messages.
        // Here we try setting interval for GLOBAL_POSITION_INT (33) to 1Hz (1000000us).
        // Param 1: Message ID (33)
        // Param 2: Interval in us (1000000)
        
        sendCommand(1, 1, MavCmd.MAV_CMD_SET_MESSAGE_INTERVAL, 33, 1000000, 0, 0, 0, 0, 0);
        
        // Also SYS_STATUS (1)
        sendCommand(1, 1, MavCmd.MAV_CMD_SET_MESSAGE_INTERVAL, 1, 1000000, 0, 0, 0, 0, 0);
    }
    
    public void sendMission(String vehicleId, List<MissionItem> mission) {
        if (mission == null || mission.isEmpty()) return;
        
        pendingMissions.put(vehicleId, mission);
        
        // Send Mission Count
        MissionCount count = MissionCount.builder()
                .targetSystem(1)
                .targetComponent(1)
                .count(mission.size())
                .missionType(MavMissionType.MAV_MISSION_TYPE_MISSION)
                .build();
                
        send(count);
        System.out.println("Sent Mission Count: " + mission.size());
    }
    
    private void sendMissionItem(String vehicleId, int seq, MissionItem item) {
        // Use fields from the uploaded item
        MissionItemInt mavItem = MissionItemInt.builder()
                .targetSystem(1)
                .targetComponent(1)
                .seq(seq)
                .frame(EnumValue.create(item.getFrame()))
                .command(EnumValue.create(item.getCommand()))
                .current(seq == 0 ? 1 : 0) // Usually 0 unless we want to jump to it immediately
                .autocontinue(item.getAutocontinue())
                .param1(item.getParam1())
                .param2(item.getParam2())
                .param3(item.getParam3())
                .param4(item.getParam4())
                .x((int)(item.getX() * 1E7)) // Lat
                .y((int)(item.getY() * 1E7)) // Lon
                .z(item.getZ())              // Alt
                .missionType(MavMissionType.MAV_MISSION_TYPE_MISSION)
                .build();

        send(mavItem);
        // System.out.println("Sent Mission Item " + seq + ": " + item.getCommand());
    }

    private void sendCommand(int targetSystem, int targetComponent, MavCmd command, 
                             float p1, float p2, float p3, float p4, float p5, float p6, float p7) {
        if (!targetIdentified) {
            System.out.println("WARN: Ignoring command " + command + " - No MAVLink target identified yet. (Waiting for Heartbeat)");
            return;
        }
        
        System.out.println("Sending Command: " + command);
        CommandLong cmd = CommandLong.builder()
                .targetSystem(targetSystem)
                .targetComponent(targetComponent)
                .command(command)
                .confirmation(0)
                .param1(p1).param2(p2).param3(p3).param4(p4).param5(p5).param6(p6).param7(p7)
                .build();
                
        send(cmd);
    }

    private void send(Object payload) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
             MavlinkConnection connection = MavlinkConnection.create(null, baos);
             connection.send2(gcsSystemId, gcsComponentId, payload);
             
             byte[] data = baos.toByteArray();
             if (targetAddress != null) {
                DatagramPacket packet = new DatagramPacket(data, data.length, targetAddress, targetPort);
                socket.send(packet);
             }
        } catch (IOException e) {
            System.err.println("Failed to send Mavlink message: " + e.getMessage());
        }
    }

    private String mapSystemIdToVehicleId(int systemId) {
        if (systemId == 1) return "scout";
        if (systemId == 2) return "delivery";
        return null; // Ignore other systems (like 255 Mission Planner)
    }

    private String mapMavState(MavState state) {
        if (state == MavState.MAV_STATE_ACTIVE) return "ARMED";
        if (state == MavState.MAV_STATE_STANDBY) return "DISARMED";
        if (state == MavState.MAV_STATE_CRITICAL) return "CRITICAL";
        if (state == MavState.MAV_STATE_EMERGENCY) return "EMERGENCY";
        return "UNKNOWN";
    }

    public Map<String, Object> getDiagnostics() {
        Map<String, Object> diag = new java.util.HashMap<>();
        diag.put("running", running);
        diag.put("targetIdentified", targetIdentified);
        diag.put("targetAddress", targetAddress != null ? targetAddress.getHostAddress() : "null");
        diag.put("targetPort", targetPort);
        diag.put("udpPort", UDP_PORT);
        diag.put("pendingMissions", pendingMissions.size());
        return diag;
    }

    @PreDestroy
    public void stop() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        if (executor != null) {
            executor.shutdown();
        }
    }
}
