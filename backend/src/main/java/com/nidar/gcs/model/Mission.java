package com.nidar.gcs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "missions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime deployedAt;

    // Mission parameters (similar to Mission Planner/QGC)
    private Double defaultAltitude; // Default altitude for all waypoints
    private Double defaultSpeed; // Default speed for mission
    private Double takeoffAltitude; // Takeoff altitude
    private Double rtlAltitude; // Return to launch altitude
    private Integer loiterRadius; // Default loiter radius
    private String missionType; // SURVEY, WAYPOINT, SEARCH, etc.

    // Geofence parameters
    private Boolean geofenceEnabled;
    private Double maxAltitude; // Maximum altitude limit
    private Double maxDistance; // Maximum distance from home

    // Camera parameters
    private Boolean cameraEnabled;
    private Integer photoInterval; // Photo interval in seconds
    private Double triggerDistance; // Distance between photos in meters

    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Waypoint> waypoints = new ArrayList<>();

    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MissionCommand> commands = new ArrayList<>();

    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RallyPoint> rallyPoints = new ArrayList<>();

    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GeofencePoint> geofencePoints = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = "CREATED";
        if (geofenceEnabled == null) {
            geofenceEnabled = false;
        }
        if (cameraEnabled == null) {
            cameraEnabled = false;
        }
    }
}
