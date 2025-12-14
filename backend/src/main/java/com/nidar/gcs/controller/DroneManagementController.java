package com.nidar.gcs.controller;

import com.nidar.gcs.model.Drone;
import com.nidar.gcs.service.DroneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drones")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:3000" })
public class DroneManagementController {

    private final DroneService droneService;

    @GetMapping
    public ResponseEntity<List<Drone>> getAllDrones() {
        return ResponseEntity.ok(droneService.getAllDrones());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Drone> getDroneById(@PathVariable @NonNull Long id) {
        Drone drone = droneService.getDroneById(id);
        if (drone != null) {
            return ResponseEntity.ok(drone);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Drone> createDrone(@RequestBody Drone drone) {
        log.info("Creating new drone: {}", drone.getName());
        Drone created = droneService.createDrone(drone);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Drone> updateDrone(@PathVariable @NonNull Long id, @RequestBody Drone drone) {
        Drone updated = droneService.updateDrone(id, drone);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteDrone(@PathVariable @NonNull Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            droneService.deleteDrone(id);
            response.put("success", true);
            response.put("message", "Drone deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/connected")
    public ResponseEntity<List<Drone>> getConnectedDrones() {
        return ResponseEntity.ok(droneService.getConnectedDrones());
    }

    @PostMapping("/{id}/position")
    public ResponseEntity<Drone> updateDronePosition(
            @PathVariable @NonNull Long id,
            @RequestBody Map<String, Double> position) {
        Drone updated = droneService.updateDronePosition(
                id,
                position.get("latitude"),
                position.get("longitude"),
                position.get("altitude"),
                position.get("battery"));
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<Drone> updateDroneStatus(
            @PathVariable @NonNull Long id,
            @RequestBody Map<String, Object> statusUpdate) {
        Drone updated = droneService.updateDroneStatus(
                id,
                (String) statusUpdate.get("status"),
                (Boolean) statusUpdate.get("connected"));
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }
}
