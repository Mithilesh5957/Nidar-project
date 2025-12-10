package com.nidar.gcs.service;

import com.nidar.gcs.model.Detection;
import com.nidar.gcs.model.TelemetryPoint;
import com.nidar.gcs.model.Vehicle;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VehicleService {

    // In-memory storage
    private final Map<String, Vehicle> vehicles = new ConcurrentHashMap<>();
    private final Map<String, List<TelemetryPoint>> telemetryHistory = new ConcurrentHashMap<>();
    private final List<Detection> detections = new ArrayList<>();

    public VehicleService() {
        // Initialize default vehicles
        vehicles.put("scout", new Vehicle("scout", "SCOUT", 0, 0, 0, 0, 100, "DISARMED", System.currentTimeMillis()));
        vehicles.put("delivery",
                new Vehicle("delivery", "DELIVERY", 0, 0, 0, 0, 100, "DISARMED", System.currentTimeMillis()));
    }

    public List<Vehicle> getAllVehicles() {
        return new ArrayList<>(vehicles.values());
    }

    public Vehicle getVehicle(String id) {
        return vehicles.get(id);
    }

    public void updateTelemetry(String id, double lat, double lon, double alt, double heading, double battery,
            String status) {
        Vehicle v = vehicles.get(id);
        if (v != null) {
            v.setLat(lat);
            v.setLon(lon);
            v.setAlt(alt);
            v.setHeading(heading);
            v.setBattery(battery);
            v.setStatus(status);
            v.setLastHeartbeat(System.currentTimeMillis());

            // Add to history
            telemetryHistory.computeIfAbsent(id, k -> new ArrayList<>())
                    .add(new TelemetryPoint(id, lat, lon, alt, System.currentTimeMillis()));
        }
    }

    public List<TelemetryPoint> getTelemetry(String id) {
        return telemetryHistory.getOrDefault(id, new ArrayList<>());
    }

    public void addDetection(Detection detection) {
        detections.add(detection);
    }

    public List<Detection> getDetections() {
        return detections;
    }

    public Detection getDetection(String id) {
        return detections.stream().filter(d -> d.getId().equals(id)).findFirst().orElse(null);
    }
}
