package com.nidar.gcs.controller;

import com.nidar.gcs.model.MissionItem;
import com.nidar.gcs.model.TelemetryPoint;
import com.nidar.gcs.model.Vehicle;
import com.nidar.gcs.service.MavlinkService;
import com.nidar.gcs.service.MissionService;
import com.nidar.gcs.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private MissionService missionService;

    @Autowired
    private MavlinkService mavlinkService;

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
            mavlinkService.sendMission(id, mission);
        }
    }
    
    @PostMapping("/{id}/command/mode")
    public void setMode(@PathVariable String id, @RequestParam String mode) {
        mavlinkService.setMode(id, mode);
    }

    @PostMapping("/{id}/mission-fetch")
    public List<MissionItem> fetchMission(@PathVariable String id) {
        // In real MAVLink integration, this might query the drone.
        // For now, return what we have in memory.
        return missionService.getMission(id);
    }

    @PostMapping("/{id}/command/rtl")
    public void sendRTL(@PathVariable String id) {
        mavlinkService.returnToLaunch(id);
    }

    @PostMapping("/{id}/command/arm")
    public void arm(@PathVariable String id) {
        mavlinkService.armVehicle(id);
    }

    @PostMapping("/{id}/command/disarm")
    public void disarm(@PathVariable String id) {
        mavlinkService.disarmVehicle(id);
    }

    @PostMapping("/{id}/command/takeoff")
    public void takeoff(@PathVariable String id, @RequestParam(defaultValue = "10") float altitude) {
        mavlinkService.takeoff(id, altitude);
    }
    
    @PostMapping("/{id}/command/goto")
    public void goToPosition(@PathVariable String id, @RequestParam double lat, @RequestParam double lon, @RequestParam(defaultValue = "10") float alt) {
        mavlinkService.reposition(id, lat, lon, alt);
    }

    @PostMapping("/{id}/command/stream")
    public void requestStream(@PathVariable String id) {
        mavlinkService.requestDataStream(id);
    }
    @GetMapping("/diagnostics")
    public java.util.Map<String, Object> getDiagnostics() {
        return mavlinkService.getDiagnostics();
    }
}
