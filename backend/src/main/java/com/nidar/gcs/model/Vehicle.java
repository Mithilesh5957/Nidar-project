package com.nidar.gcs.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Vehicle {
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
