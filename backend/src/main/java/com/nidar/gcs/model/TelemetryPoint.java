package com.nidar.gcs.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TelemetryPoint {
    private String vehicleId;
    private double lat;
    private double lon;
    private double alt;
    private long timestamp;
}
