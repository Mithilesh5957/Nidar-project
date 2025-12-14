package com.nidar.gcs.service;

import com.nidar.gcs.model.Mission;
import com.nidar.gcs.model.Telemetry;
import com.nidar.gcs.repository.MissionRepository;
import com.nidar.gcs.repository.TelemetryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MissionReplayService {

    private final MissionRepository missionRepository;
    private final TelemetryRepository telemetryRepository;

    public Map<String, Object> getMissionReplayData(@NonNull Long missionId) {
        Map<String, Object> result = new HashMap<>();

        Mission mission = missionRepository.findById(missionId).orElse(null);
        if (mission == null) {
            result.put("error", "Mission not found");
            return result;
        }

        // Get all telemetry data for the mission sorted by timestamp
        List<Telemetry> telemetryData = telemetryRepository.findAll(Sort.by(Sort.Direction.ASC, "timestamp"));

        // Filter telemetry for this mission if mission reference exists
        List<Telemetry> missionTelemetry = telemetryData.stream()
                .filter(t -> t.getMission() != null && t.getMission().getId().equals(missionId))
                .toList();

        result.put("mission", mission);
        result.put("telemetry", missionTelemetry);
        result.put("totalPoints", missionTelemetry.size());

        // Calculate mission statistics
        if (!missionTelemetry.isEmpty()) {
            Map<String, Object> stats = calculateMissionStatistics(missionTelemetry);
            result.put("statistics", stats);
        }

        return result;
    }

    public List<Telemetry> getTelemetryByMission(@NonNull Long missionId) {
        Mission mission = missionRepository.findById(missionId).orElse(null);
        if (mission == null) {
            return List.of();
        }

        List<Telemetry> allTelemetry = telemetryRepository.findAll(Sort.by(Sort.Direction.ASC, "timestamp"));
        return allTelemetry.stream()
                .filter(t -> t.getMission() != null && t.getMission().getId().equals(missionId))
                .toList();
    }

    private Map<String, Object> calculateMissionStatistics(List<Telemetry> telemetry) {
        Map<String, Object> stats = new HashMap<>();

        if (telemetry.isEmpty()) {
            return stats;
        }

        // Calculate max altitude
        double maxAltitude = telemetry.stream()
                .mapToDouble(t -> t.getAltitude() != null ? t.getAltitude() : 0.0)
                .max()
                .orElse(0.0);

        // Calculate max speed
        double maxSpeed = telemetry.stream()
                .mapToDouble(t -> t.getSpeed() != null ? t.getSpeed() : 0.0)
                .max()
                .orElse(0.0);

        // Calculate average speed
        double avgSpeed = telemetry.stream()
                .mapToDouble(t -> t.getSpeed() != null ? t.getSpeed() : 0.0)
                .average()
                .orElse(0.0);

        // Calculate total distance (simplified)
        double totalDistance = 0.0;
        for (int i = 1; i < telemetry.size(); i++) {
            Telemetry prev = telemetry.get(i - 1);
            Telemetry curr = telemetry.get(i);

            if (prev.getLatitude() != null && prev.getLongitude() != null &&
                    curr.getLatitude() != null && curr.getLongitude() != null) {
                totalDistance += calculateDistance(
                        prev.getLatitude(), prev.getLongitude(),
                        curr.getLatitude(), curr.getLongitude());
            }
        }

        // Battery usage
        Telemetry first = telemetry.get(0);
        Telemetry last = telemetry.get(telemetry.size() - 1);
        double batteryUsed = (first.getBattery() != null && last.getBattery() != null)
                ? first.getBattery() - last.getBattery()
                : 0.0;

        stats.put("maxAltitude", maxAltitude);
        stats.put("maxSpeed", maxSpeed);
        stats.put("avgSpeed", avgSpeed);
        stats.put("totalDistance", totalDistance);
        stats.put("batteryUsed", batteryUsed);
        stats.put("duration", telemetry.size()); // in seconds (assuming 1Hz telemetry)
        stats.put("startTime", first.getTimestamp());
        stats.put("endTime", last.getTimestamp());

        return stats;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula to calculate distance between two GPS coordinates
        double R = 6371000; // Earth radius in meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
