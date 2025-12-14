package com.nidar.gcs.controller;

import com.nidar.gcs.model.GeofenceZone;
import com.nidar.gcs.service.GeofenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/geofence")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:3000" })
public class GeofenceController {

    private final GeofenceService geofenceService;

    @GetMapping
    public ResponseEntity<List<GeofenceZone>> getAllZones() {
        return ResponseEntity.ok(geofenceService.getAllZones());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeofenceZone> getZoneById(@PathVariable @NonNull Long id) {
        GeofenceZone zone = geofenceService.getZoneById(id);
        if (zone != null) {
            return ResponseEntity.ok(zone);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/mission/{missionId}")
    public ResponseEntity<List<GeofenceZone>> getZonesByMission(@PathVariable @NonNull Long missionId) {
        return ResponseEntity.ok(geofenceService.getZonesByMission(missionId));
    }

    @PostMapping
    public ResponseEntity<GeofenceZone> createZone(@RequestBody GeofenceZone zone) {
        log.info("Creating geofence zone: {}", zone.getName());
        GeofenceZone created = geofenceService.createZone(zone);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GeofenceZone> updateZone(@PathVariable @NonNull Long id, @RequestBody GeofenceZone zone) {
        GeofenceZone updated = geofenceService.updateZone(id, zone);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteZone(@PathVariable @NonNull Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            geofenceService.deleteZone(id);
            response.put("success", true);
            response.put("message", "Geofence zone deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validatePosition(@RequestBody Map<String, Object> request) {
        try {
            Double latitude = request.get("latitude") != null ? ((Number) request.get("latitude")).doubleValue() : null;
            Double longitude = request.get("longitude") != null ? ((Number) request.get("longitude")).doubleValue()
                    : null;
            Double altitude = request.get("altitude") != null ? ((Number) request.get("altitude")).doubleValue() : null;
            Long missionId = request.get("missionId") != null ? ((Number) request.get("missionId")).longValue() : null;

            if (latitude == null || longitude == null || altitude == null || missionId == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("valid", false);
                error.put("message", "Missing required parameters");
                return ResponseEntity.badRequest().body(error);
            }

            Map<String, Object> result = geofenceService.validatePosition(latitude, longitude, altitude, missionId);
            return ResponseEntity.ok(result);
        } catch (ClassCastException e) {
            log.error("Invalid parameter types", e);
            Map<String, Object> error = new HashMap<>();
            error.put("valid", false);
            error.put("message", "Invalid parameter types");
            return ResponseEntity.badRequest().body(error);
        }
    }
}
