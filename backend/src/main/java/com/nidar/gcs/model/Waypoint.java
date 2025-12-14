package com.nidar.gcs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "waypoints")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Waypoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer sequence;
    private Double latitude;
    private Double longitude;
    private Double altitude;
    private String command;

    // Additional parameters for advanced mission planning
    private Double speed; // Target speed at waypoint (m/s)
    private Double heading; // Target heading (degrees)
    private Integer delay; // Delay at waypoint (seconds)
    private Integer acceptanceRadius; // Radius for waypoint acceptance (meters)
    private Integer passRadius; // Radius to pass by waypoint (meters)
    private Double yaw; // Yaw angle (degrees)
    private Integer frame; // Coordinate frame (MAV_FRAME)
    private Integer autocontinue; // Auto continue to next waypoint (0/1)

    // Camera control
    private Integer cameraAction; // Camera action at waypoint
    private Integer cameraPitch; // Camera pitch angle
    private Integer cameraYaw; // Camera yaw angle

    @ManyToOne
    @JoinColumn(name = "mission_id")
    private Mission mission;
}
