package com.nidar.gcs.service;

import com.nidar.gcs.model.Detection;
import com.nidar.gcs.model.TelemetryPoint;
import com.nidar.gcs.model.Vehicle;
import com.nidar.gcs.repository.DetectionRepository;
import com.nidar.gcs.repository.TelemetryPointRepository;
import com.nidar.gcs.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Service
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepo;

    @Autowired
    private TelemetryPointRepository telemetryRepo;

    @Autowired
    private DetectionRepository detectionRepo;

    @PostConstruct
    public void init() {
        // Initialize default vehicles if not present
        if (!vehicleRepo.existsById("scout")) {
            vehicleRepo.save(new Vehicle("scout", "SCOUT", 0, 0, 0, 0, 100, "DISARMED", System.currentTimeMillis()));
        }
        if (!vehicleRepo.existsById("delivery")) {
            vehicleRepo
                    .save(new Vehicle("delivery", "DELIVERY", 0, 0, 0, 0, 100, "DISARMED", System.currentTimeMillis()));
        }
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleRepo.findAll();
    }

    public Vehicle getVehicle(String id) {
        return vehicleRepo.findById(id).orElse(null);
    }

    public void updateTelemetry(String id, double lat, double lon, double alt, double heading, double battery,
            String status) {
        Vehicle v = vehicleRepo.findById(id).orElse(null);
        if (v != null) {
            v.setLat(lat);
            v.setLon(lon);
            v.setAlt(alt);
            v.setHeading(heading);
            v.setBattery(battery);
            v.setStatus(status);
            v.setLastHeartbeat(System.currentTimeMillis());

            vehicleRepo.save(v); // Update current state

            // Log history
            telemetryRepo.save(new TelemetryPoint(id, lat, lon, alt, System.currentTimeMillis()));
        }
    }

    public List<TelemetryPoint> getTelemetry(String id) {
        return telemetryRepo.findByVehicleId(id);
    }

    public void addDetection(Detection detection) {
        detectionRepo.save(detection);
    }

    public List<Detection> getDetections() {
        return detectionRepo.findAll();
    }

    public Detection getDetection(String id) {
        return detectionRepo.findById(id).orElse(null);
    }
}
