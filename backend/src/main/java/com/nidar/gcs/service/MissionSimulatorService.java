package com.nidar.gcs.service;

import com.nidar.gcs.model.Mission;
import com.nidar.gcs.model.Waypoint;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for simulating and validating missions before deployment
 */
@Service
@Slf4j
public class MissionSimulatorService {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double MAX_SAFE_ALTITUDE = 120.0; // meters (FAA limit)
    private static final double MIN_WAYPOINT_DISTANCE = 5.0; // meters

    @Data
    public static class SimulationResult {
        private boolean valid;
        private List<String> errors;
        private List<String> warnings;
        private double totalDistance; // meters
        private double estimatedFlightTime; // seconds
        private double estimatedBatteryUsage; // percentage
        private double maxAltitude; // meters
        private double avgSpeed; // m/s
        private int waypointCount;

        public SimulationResult() {
            this.errors = new ArrayList<>();
            this.warnings = new ArrayList<>();
        }
    }

    /**
     * Simulate and validate a mission
     */
    public SimulationResult simulateMission(Mission mission) {
        SimulationResult result = new SimulationResult();

        List<Waypoint> waypoints = mission.getWaypoints();
        if (waypoints == null || waypoints.isEmpty()) {
            result.setValid(false);
            result.getErrors().add("Mission has no waypoints");
            return result;
        }

        result.setWaypointCount(waypoints.size());

        // Validate each waypoint
        for (int i = 0; i < waypoints.size(); i++) {
            Waypoint wp = waypoints.get(i);
            validateWaypoint(wp, i, result);
        }

        // Calculate mission metrics
        calculateDistance(waypoints, result);
        calculateFlightTime(waypoints, mission, result);
        calculateBatteryUsage(result);
        checkAltitudeChanges(waypoints, result);
        checkWaypointSpacing(waypoints, result);
        checkGeofence(waypoints, mission, result);

        // Determine if valid
        result.setValid(result.getErrors().isEmpty());

        log.info("Mission simulation complete: valid={}, distance={}m, time={}s",
                result.isValid(), result.getTotalDistance(), result.getEstimatedFlightTime());

        return result;
    }

    private void validateWaypoint(Waypoint wp, int index, SimulationResult result) {
        if (wp.getLatitude() < -90 || wp.getLatitude() > 90) {
            result.getErrors().add(String.format("WP%d: Invalid latitude %.6f", index, wp.getLatitude()));
        }
        if (wp.getLongitude() < -180 || wp.getLongitude() > 180) {
            result.getErrors().add(String.format("WP%d: Invalid longitude %.6f", index, wp.getLongitude()));
        }

        if (wp.getAltitude() < 0) {
            result.getErrors().add(String.format("WP%d: Negative altitude %.1fm", index, wp.getAltitude()));
        }
        if (wp.getAltitude() > MAX_SAFE_ALTITUDE) {
            result.getWarnings().add(String.format("WP%d: Altitude %.1fm exceeds safe limit of %.1fm",
                    index, wp.getAltitude(), MAX_SAFE_ALTITUDE));
        }

        if (wp.getAltitude() > result.getMaxAltitude()) {
            result.setMaxAltitude(wp.getAltitude());
        }
    }

    private void calculateDistance(List<Waypoint> waypoints, SimulationResult result) {
        double totalDistance = 0;

        for (int i = 0; i < waypoints.size() - 1; i++) {
            Waypoint wp1 = waypoints.get(i);
            Waypoint wp2 = waypoints.get(i + 1);

            double distance = calculateDistance(
                    wp1.getLatitude(), wp1.getLongitude(), wp1.getAltitude(),
                    wp2.getLatitude(), wp2.getLongitude(), wp2.getAltitude());

            totalDistance += distance;
        }

        result.setTotalDistance(totalDistance);
    }

    private double calculateDistance(double lat1, double lon1, double alt1,
            double lat2, double lon2, double alt2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double horizontalDistance = EARTH_RADIUS_KM * c * 1000;

        double altitudeDiff = alt2 - alt1;
        return Math.sqrt(horizontalDistance * horizontalDistance + altitudeDiff * altitudeDiff);
    }

    private void calculateFlightTime(List<Waypoint> waypoints, Mission mission, SimulationResult result) {
        double totalTime = 0;
        double defaultSpeed = mission.getDefaultSpeed() != null ? mission.getDefaultSpeed() : 10.0;

        for (int i = 0; i < waypoints.size() - 1; i++) {
            Waypoint wp1 = waypoints.get(i);
            Waypoint wp2 = waypoints.get(i + 1);

            double distance = calculateDistance(
                    wp1.getLatitude(), wp1.getLongitude(), wp1.getAltitude(),
                    wp2.getLatitude(), wp2.getLongitude(), wp2.getAltitude());

            double speed = wp2.getSpeed() != null ? wp2.getSpeed() : defaultSpeed;
            totalTime += distance / speed;

            if (wp2.getDelay() != null) {
                totalTime += wp2.getDelay();
            }
        }

        result.setEstimatedFlightTime(totalTime);
        result.setAvgSpeed(result.getTotalDistance() / totalTime);
    }

    private void calculateBatteryUsage(SimulationResult result) {
        double maxFlightTime = 20 * 60; // 20 minutes in seconds
        double batteryUsage = (result.getEstimatedFlightTime() / maxFlightTime) * 100;
        batteryUsage *= 1.2; // 20% overhead

        result.setEstimatedBatteryUsage(Math.min(batteryUsage, 100));

        if (batteryUsage > 80) {
            result.getWarnings().add("Mission may require more than 80% battery");
        }
        if (batteryUsage > 100) {
            result.getErrors().add("Mission estimated to require more than 100% battery");
        }
    }

    private void checkAltitudeChanges(List<Waypoint> waypoints, SimulationResult result) {
        for (int i = 0; i < waypoints.size() - 1; i++) {
            Waypoint wp1 = waypoints.get(i);
            Waypoint wp2 = waypoints.get(i + 1);

            double altChange = wp2.getAltitude() - wp1.getAltitude();
            double horizontalDist = calculateDistance(
                    wp1.getLatitude(), wp1.getLongitude(), 0,
                    wp2.getLatitude(), wp2.getLongitude(), 0);

            if (horizontalDist > 0) {
                double climbAngle = Math.toDegrees(Math.atan(Math.abs(altChange) / horizontalDist));
                if (climbAngle > 45) {
                    result.getWarnings().add(String.format(
                            "WP%d to WP%d: Steep altitude change (%.1f degrees)",
                            i, i + 1, climbAngle));
                }
            }
        }
    }

    private void checkWaypointSpacing(List<Waypoint> waypoints, SimulationResult result) {
        for (int i = 0; i < waypoints.size() - 1; i++) {
            Waypoint wp1 = waypoints.get(i);
            Waypoint wp2 = waypoints.get(i + 1);

            double distance = calculateDistance(
                    wp1.getLatitude(), wp1.getLongitude(), 0,
                    wp2.getLatitude(), wp2.getLongitude(), 0);

            if (distance < MIN_WAYPOINT_DISTANCE) {
                result.getWarnings().add(String.format(
                        "WP%d to WP%d: Waypoints very close (%.1fm)",
                        i, i + 1, distance));
            }
        }
    }

    private void checkGeofence(List<Waypoint> waypoints, Mission mission, SimulationResult result) {
        if (!Boolean.TRUE.equals(mission.getGeofenceEnabled())) {
            return;
        }

        Double maxAlt = mission.getMaxAltitude();
        if (maxAlt != null) {
            for (int i = 0; i < waypoints.size(); i++) {
                if (waypoints.get(i).getAltitude() > maxAlt) {
                    result.getErrors().add(String.format(
                            "WP%d: Altitude %.1fm exceeds geofence limit %.1fm",
                            i, waypoints.get(i).getAltitude(), maxAlt));
                }
            }
        }
    }
}
