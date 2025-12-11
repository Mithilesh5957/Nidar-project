package com.nidar.gcs.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "telemetry_log")
public class TelemetryPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    private String vehicleId;
    private double lat;
    private double lon;
    private double alt;
    private long timestamp;

    public TelemetryPoint(String vehicleId, double lat, double lon, double alt, long timestamp) {
        this.vehicleId = vehicleId;
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
        this.timestamp = timestamp;
    }
}
