package com.nidar.gcs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "drones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Drone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String serialNumber;
    private String model;
    private String status; // IDLE, FLYING, MAINTENANCE, OFFLINE
    private Boolean connected;

    // Connection details
    private String mavproxyHost;
    private Integer mavproxyPort;

    // Latest position
    private Double lastLatitude;
    private Double lastLongitude;
    private Double lastAltitude;
    private Double lastBattery;

    private LocalDateTime lastSeenAt;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (connected == null) {
            connected = false;
        }
        if (status == null) {
            status = "OFFLINE";
        }
    }
}
