package com.nidar.gcs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "flight_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "mission_id")
    private Mission mission;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationSeconds;

    // Flight statistics
    private Double maxAltitude;
    private Double maxSpeed;
    private Double totalDistance;
    private Double avgSpeed;

    // Battery statistics
    private Double startBattery;
    private Double endBattery;
    private Double batteryUsed;

    // Waypoint completion
    private Integer waypointsPlanned;
    private Integer waypointsCompleted;

    // Status
    private String flightStatus; // COMPLETED, ABORTED, EMERGENCY_LANDED, etc.
    private String notes;

    // Geolocation data
    private Double homeLatitude;
    private Double homeLongitude;
    private Double maxDistanceFromHome;

    @PrePersist
    protected void onCreate() {
        if (startTime == null) {
            startTime = LocalDateTime.now();
        }
    }
}
