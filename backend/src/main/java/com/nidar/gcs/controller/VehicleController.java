package com.nidar.gcs.controller;

import com.nidar.gcs.model.MissionItem;
import com.nidar.gcs.model.TelemetryPoint;
import com.nidar.gcs.model.Vehicle;
import com.nidar.gcs.service.MAVProxyService;
import com.nidar.gcs.service.MissionService;
import com.nidar.gcs.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:3000" })
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private MissionService missionService;

    @Autowired
    private MAVProxyService mavProxyService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping
    public List<Vehicle> getVehicles() {
        return vehicleService.getAllVehicles();
    }

    @PostMapping("/{id}/telemetry")
    public void pushTelemetry(@PathVariable String id, @RequestBody Vehicle telemetry) {
        vehicleService.updateTelemetry(id, telemetry.getLat(), telemetry.getLon(), telemetry.getAlt(),
                telemetry.getHeading(), telemetry.getBattery(), telemetry.getStatus());

        // Broadcast to WebSocket
        Vehicle v = vehicleService.getVehicle(id);
        if (v != null) {
            messagingTemplate.convertAndSend("/topic/telemetry/" + id, v);
        }
    }

    @GetMapping("/{id}/telemetry-history")
    public List<TelemetryPoint> getTelemetryHistory(@PathVariable String id) {
        return vehicleService.getTelemetry(id);
    }

    @PostMapping("/{id}/mission-upload")
    public void uploadMission(@PathVariable String id, @RequestBody List<MissionItem> mission) {
        if (mission != null) {
            missionService.setMission(id, mission);
            messagingTemplate.convertAndSend("/topic/missions/" + id, mission);
            // Mission is saved to DB; MAVProxy upload happens via DroneController
        }
    }

    @PostMapping("/{id}/command/mode")
    public Map<String, Object> setMode(@PathVariable String id, @RequestParam String mode) {
        Map<String, Object> response = new HashMap<>();
        // Mode commands sent via MAVProxyService.sendCommand
        boolean success = mavProxyService.sendCommand("MODE " + mode);
        response.put("success", success);
        response.put("mode", mode);
        return response;
    }

    @PostMapping("/{id}/mission-fetch")
    public List<MissionItem> fetchMission(@PathVariable String id) {
        return missionService.getMission(id);
    }

    @PostMapping("/{id}/command/rtl")
    public Map<String, Object> sendRTL(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        boolean success = mavProxyService.sendCommand("RTL");
        response.put("success", success);
        response.put("command", "RTL");
        return response;
    }

    @PostMapping("/{id}/command/arm")
    public Map<String, Object> arm(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        boolean success = mavProxyService.sendCommand("ARM");
        response.put("success", success);
        response.put("command", "ARM");
        return response;
    }

    @PostMapping("/{id}/command/disarm")
    public Map<String, Object> disarm(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        boolean success = mavProxyService.sendCommand("DISARM");
        response.put("success", success);
        response.put("command", "DISARM");
        return response;
    }

    @PostMapping("/{id}/command/takeoff")
    public Map<String, Object> takeoff(@PathVariable String id, @RequestParam(defaultValue = "10") float altitude) {
        Map<String, Object> response = new HashMap<>();
        boolean success = mavProxyService.sendCommand("TAKEOFF " + altitude);
        response.put("success", success);
        response.put("command", "TAKEOFF");
        response.put("altitude", altitude);
        return response;
    }

    @PostMapping("/{id}/command/goto")
    public Map<String, Object> goToPosition(@PathVariable String id, @RequestParam double lat, @RequestParam double lon,
            @RequestParam(defaultValue = "10") float alt) {
        Map<String, Object> response = new HashMap<>();
        boolean success = mavProxyService.sendCommand("GOTO " + lat + " " + lon + " " + alt);
        response.put("success", success);
        response.put("command", "GOTO");
        response.put("lat", lat);
        response.put("lon", lon);
        response.put("alt", alt);
        return response;
    }

    @PostMapping("/{id}/command/stream")
    public Map<String, Object> requestStream(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        boolean success = mavProxyService.sendCommand("REQUEST_DATA_STREAM");
        response.put("success", success);
        response.put("command", "REQUEST_DATA_STREAM");
        return response;
    }

    @GetMapping("/diagnostics")
    public Map<String, Object> getDiagnostics() {
        Map<String, Object> diagnostics = new HashMap<>();
        diagnostics.put("connected", mavProxyService.isConnected());
        diagnostics.put("timestamp", System.currentTimeMillis());
        return diagnostics;
    }
}
