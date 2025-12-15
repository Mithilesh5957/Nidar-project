package com.nidar.gcs.service;

import com.nidar.gcs.model.Mission;
import com.nidar.gcs.model.Waypoint;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for executing missions by navigating through waypoints
 * Provides realistic flight simulation with smooth transitions
 */
@Service
@Slf4j
public class MissionExecutionService {

    @Value("${mission.execution.default-speed:10.0}")
    private double defaultSpeed;

    @Value("${mission.execution.start-position.latitude:40.7128}")
    private double startLatitude;

    @Value("${mission.execution.start-position.longitude:-74.0060}")
    private double startLongitude;

    private Mission activeMission;
    private MissionState currentState;
    private int currentWaypointIndex = 0;
    private double currentLatitude;
    private double currentLongitude;
    private double currentAltitude = 0.0;
    private double currentHeading = 0.0;
    private double currentSpeed = 0.0;
    private double batteryLevel = 100.0;
    private boolean armed = false;
    private String flightMode = "STABILIZE";
    private LocalDateTime lastUpdateTime;
    private boolean missionCompleted = false;

    // Progress through current leg (0.0 to 1.0)
    private double legProgress = 0.0;
    private Waypoint currentWaypoint;
    private Waypoint nextWaypoint;

    public enum MissionState {
        IDLE,
        ARMED,
        TAKING_OFF,
        EXECUTING,
        PAUSED,
        RETURNING_HOME,
        LANDING,
        COMPLETED
    }

    @Data
    public static class ExecutionStatus {
        private MissionState state;
        private String missionName;
        private int currentWaypoint;
        private int totalWaypoints;
        private double distanceToNext;
        private double batteryRemaining;
        private boolean armed;
        private String flightMode;
        private double latitude;
        private double longitude;
        private double altitude;
        private double heading;
        private double speed;
        private double progress; // 0-100%
    }

    public MissionExecutionService() {
        // Fields initialized after @Value annotation injection
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        this.currentState = MissionState.IDLE;
        this.currentLatitude = startLatitude;
        this.currentLongitude = startLongitude;
        this.lastUpdateTime = LocalDateTime.now();
        log.info("MissionExecutionService initialized at start position: {}, {}", startLatitude, startLongitude);
    }

    /**
     * Start executing a mission
     */
    public boolean startMission(Mission mission) {
        if (mission == null || mission.getWaypoints() == null || mission.getWaypoints().isEmpty()) {
            log.error("Cannot start mission: Mission or waypoints are null/empty");
            return false;
        }

        log.info("Starting mission: {}", mission.getName());
        this.activeMission = mission;
        this.currentWaypointIndex = 0;
        this.currentState = MissionState.ARMED;
        this.flightMode = "GUIDED";
        this.armed = true;
        this.batteryLevel = 100.0;
        this.missionCompleted = false;
        this.legProgress = 0.0;
        this.lastUpdateTime = LocalDateTime.now();

        // Set first and second waypoints
        this.currentWaypoint = null; // Start from current position
        this.nextWaypoint = mission.getWaypoints().get(0);

        // Transition to executing after arm
        this.currentState = MissionState.EXECUTING;
        this.flightMode = "AUTO";

        log.info("Mission started with {} waypoints", mission.getWaypoints().size());
        return true;
    }

    /**
     * Update mission execution - called periodically
     */
    public void update() {
        if (currentState != MissionState.EXECUTING || activeMission == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        double deltaTime = java.time.Duration.between(lastUpdateTime, now).toMillis() / 1000.0;
        lastUpdateTime = now;

        if (deltaTime <= 0 || deltaTime > 2.0) {
            // Skip invalid time deltas
            return;
        }

        // Get current mission speed
        double speed = defaultSpeed;
        if (nextWaypoint != null && nextWaypoint.getSpeed() != null) {
            speed = nextWaypoint.getSpeed();
        } else if (activeMission.getDefaultSpeed() != null) {
            speed = activeMission.getDefaultSpeed();
        }
        currentSpeed = speed;

        // Calculate distance traveled in this update
        double distanceTraveled = speed * deltaTime;

        // Navigate to next waypoint
        if (nextWaypoint != null) {
            double targetLat = nextWaypoint.getLatitude();
            double targetLon = nextWaypoint.getLongitude();
            double targetAlt = nextWaypoint.getAltitude();

            // Calculate total distance to waypoint
            double totalDistance = calculateDistance(currentLatitude, currentLongitude, currentAltitude,
                    targetLat, targetLon, targetAlt);

            if (totalDistance < 1.0) {
                // Reached waypoint
                log.info("Reached waypoint {}: {},{},{}", currentWaypointIndex,
                        targetLat, targetLon, targetAlt);

                currentLatitude = targetLat;
                currentLongitude = targetLon;
                currentAltitude = targetAlt;

                // Move to next waypoint
                currentWaypointIndex++;
                if (currentWaypointIndex >= activeMission.getWaypoints().size()) {
                    // Mission completed
                    completeMission();
                    return;
                } else {
                    currentWaypoint = nextWaypoint;
                    nextWaypoint = activeMission.getWaypoints().get(currentWaypointIndex);
                    legProgress = 0.0;
                }
            } else {
                // Move towards waypoint
                double fraction = Math.min(distanceTraveled / totalDistance, 1.0);
                legProgress += fraction;

                // Interpolate position
                currentLatitude += (targetLat - currentLatitude) * fraction;
                currentLongitude += (targetLon - currentLongitude) * fraction;
                currentAltitude += (targetAlt - currentAltitude) * fraction;

                // Calculate heading
                currentHeading = calculateBearing(currentLatitude, currentLongitude, targetLat, targetLon);
            }
        }

        // Simulate battery drain (0.1% per second at default speed)
        batteryLevel -= 0.1 * deltaTime;
        if (batteryLevel < 0) {
            batteryLevel = 0;
        }
    }

    /**
     * Pause mission execution
     */
    public void pauseMission() {
        if (currentState == MissionState.EXECUTING) {
            currentState = MissionState.PAUSED;
            flightMode = "LOITER";
            log.info("Mission paused at waypoint {}", currentWaypointIndex);
        }
    }

    /**
     * Resume mission execution
     */
    public void resumeMission() {
        if (currentState == MissionState.PAUSED) {
            currentState = MissionState.EXECUTING;
            flightMode = "AUTO";
            lastUpdateTime = LocalDateTime.now();
            log.info("Mission resumed at waypoint {}", currentWaypointIndex);
        }
    }

    /**
     * Stop mission execution
     */
    public void stopMission() {
        log.info("Mission stopped");
        activeMission = null;
        currentState = MissionState.IDLE;
        flightMode = "STABILIZE";
        armed = false;
        currentWaypointIndex = 0;
        legProgress = 0.0;
        missionCompleted = false;
    }

    /**
     * Complete mission
     */
    private void completeMission() {
        log.info("Mission completed successfully!");
        currentState = MissionState.COMPLETED;
        flightMode = "LOITER";
        missionCompleted = true;
    }

    /**
     * ARM the drone
     */
    public boolean arm() {
        if (!armed) {
            armed = true;
            currentState = MissionState.ARMED;
            flightMode = "STABILIZE";
            log.info("Drone armed");
            return true;
        }
        return false;
    }

    /**
     * DISARM the drone
     */
    public boolean disarm() {
        if (armed && currentState != MissionState.EXECUTING) {
            armed = false;
            currentState = MissionState.IDLE;
            flightMode = "STABILIZE";
            log.info("Drone disarmed");
            return true;
        }
        return false;
    }

    /**
     * Get current execution status
     */
    public ExecutionStatus getStatus() {
        ExecutionStatus status = new ExecutionStatus();
        status.setState(currentState);
        status.setMissionName(activeMission != null ? activeMission.getName() : null);
        status.setCurrentWaypoint(currentWaypointIndex);
        status.setTotalWaypoints(activeMission != null ? activeMission.getWaypoints().size() : 0);
        status.setBatteryRemaining(batteryLevel);
        status.setArmed(armed);
        status.setFlightMode(flightMode);
        status.setLatitude(currentLatitude);
        status.setLongitude(currentLongitude);
        status.setAltitude(currentAltitude);
        status.setHeading(currentHeading);
        status.setSpeed(currentSpeed);

        // Calculate progress
        if (activeMission != null && !activeMission.getWaypoints().isEmpty()) {
            double progress = (currentWaypointIndex * 100.0) / activeMission.getWaypoints().size();
            status.setProgress(Math.min(progress, 100.0));
        } else {
            status.setProgress(0.0);
        }

        // Calculate distance to next waypoint
        if (nextWaypoint != null) {
            status.setDistanceToNext(calculateDistance(
                    currentLatitude, currentLongitude, currentAltitude,
                    nextWaypoint.getLatitude(), nextWaypoint.getLongitude(), nextWaypoint.getAltitude()));
        }

        return status;
    }

    /**
     * Get current position for telemetry
     */
    public double getLatitude() {
        return currentLatitude;
    }

    public double getLongitude() {
        return currentLongitude;
    }

    public double getAltitude() {
        return currentAltitude;
    }

    public double getHeading() {
        return currentHeading;
    }

    public double getSpeed() {
        return currentSpeed;
    }

    public double getBattery() {
        return batteryLevel;
    }

    public boolean isArmed() {
        return armed;
    }

    public String getFlightMode() {
        return flightMode;
    }

    public boolean isMissionActive() {
        return activeMission != null && currentState == MissionState.EXECUTING;
    }

    public Mission getActiveMission() {
        return activeMission;
    }

    public MissionState getCurrentState() {
        return currentState;
    }

    /**
     * Calculate distance between two points (Haversine formula + altitude)
     */
    private double calculateDistance(double lat1, double lon1, double alt1,
            double lat2, double lon2, double alt2) {
        final double R = 6371000; // Earth radius in meters

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double horizontalDistance = R * c;

        double altitudeDiff = alt2 - alt1;
        return Math.sqrt(horizontalDistance * horizontalDistance + altitudeDiff * altitudeDiff);
    }

    /**
     * Calculate bearing between two points
     */
    private double calculateBearing(double lat1, double lon1, double lat2, double lon2) {
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) -
                Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);

        double bearing = Math.toDegrees(Math.atan2(y, x));
        return (bearing + 360) % 360;
    }
}
