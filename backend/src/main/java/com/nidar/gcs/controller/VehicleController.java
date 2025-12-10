package com.nidar.gcs.controller;

import com.nidar.gcs.model.MissionItem;
import com.nidar.gcs.model.TelemetryPoint;
import com.nidar.gcs.model.Vehicle;
import com.nidar.gcs.service.MissionService;
import com.nidar.gcs.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private MissionService missionService;

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
        messagingTemplate.convertAndSend("/topic/telemetry/" + id, vehicleService.getVehicle(id));
    }

    @GetMapping("/{id}/telemetry-history")
    public List<TelemetryPoint> getTelemetryHistory(@PathVariable String id) {
        return vehicleService.getTelemetry(id);
    }

    @PostMapping("/{id}/mission-upload")
    public void uploadMission(@PathVariable String id, @RequestBody List<MissionItem> mission) {
        missionService.setMission(id, mission);
        messagingTemplate.convertAndSend("/topic/missions/" + id, mission);
        // TODO: In real MAVLink integration, this would send to MAVLink agent
    }

    @PostMapping("/{id}/mission-fetch")
    public List<MissionItem> fetchMission(@PathVariable String id) {
        // TODO: In real MAVLink integration, this might query the drone.
        // For now, return what we have in memory.
        return missionService.getMission(id);
    }

    @PostMapping("/{id}/command/rtl")
    public void sendRTL(@PathVariable String id) {
        // TODO: Forward to MAVLink
        System.out.println("Sending RTL command to " + id);
        // Broadcast status update for simulation
        vehicleService.updateTelemetry(id, 0, 0, 0, 0, 0, "RTL"); // Not resetting coords, just status logic
        // Only update status string in reality
        Vehicle v = vehicleService.getVehicle(id);
        if (v != null) {
            v.setStatus("RTL");
            messagingTemplate.convertAndSend("/topic/telemetry/" + id, v);
        }
    }
}
