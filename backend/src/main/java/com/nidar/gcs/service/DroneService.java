package com.nidar.gcs.service;

import com.nidar.gcs.model.Drone;
import com.nidar.gcs.repository.DroneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DroneService {

    private final DroneRepository droneRepository;

    public List<Drone> getAllDrones() {
        return droneRepository.findAll();
    }

    public Drone getDroneById(@NonNull Long id) {
        return droneRepository.findById(id).orElse(null);
    }

    public Drone createDrone(Drone drone) {
        log.info("Creating new drone: {}", drone.getName());
        return droneRepository.save(drone);
    }

    public Drone updateDrone(@NonNull Long id, Drone droneDetails) {
        Drone drone = getDroneById(id);
        if (drone != null) {
            drone.setName(droneDetails.getName());
            drone.setSerialNumber(droneDetails.getSerialNumber());
            drone.setModel(droneDetails.getModel());
            drone.setStatus(droneDetails.getStatus());
            drone.setConnected(droneDetails.getConnected());
            drone.setMavproxyHost(droneDetails.getMavproxyHost());
            drone.setMavproxyPort(droneDetails.getMavproxyPort());
            return droneRepository.save(drone);
        }
        return null;
    }

    public void deleteDrone(@NonNull Long id) {
        droneRepository.deleteById(id);
    }

    public List<Drone> getConnectedDrones() {
        return droneRepository.findByConnected(true);
    }

    public Drone updateDronePosition(@NonNull Long id, Double latitude, Double longitude, Double altitude,
            Double battery) {
        Drone drone = getDroneById(id);
        if (drone != null) {
            drone.setLastLatitude(latitude);
            drone.setLastLongitude(longitude);
            drone.setLastAltitude(altitude);
            drone.setLastBattery(battery);
            drone.setLastSeenAt(LocalDateTime.now());
            return droneRepository.save(drone);
        }
        return null;
    }

    public Drone updateDroneStatus(@NonNull Long id, String status, Boolean connected) {
        Drone drone = getDroneById(id);
        if (drone != null) {
            drone.setStatus(status);
            drone.setConnected(connected);
            drone.setLastSeenAt(LocalDateTime.now());
            return droneRepository.save(drone);
        }
        return null;
    }
}
