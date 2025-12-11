package com.nidar.gcs.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "vehicles")
public class Vehicle {
    @Id
    private String id; // e.g., "scout", "delivery"
    private String type; // "SCOUT" or "DELIVERY"
    private double lat;
    private double lon;
    private double alt;
    private double heading;
    private double battery; // Percentage
    private String status; // ARMED, DISARMED, FLYING
    private long lastHeartbeat;
}
