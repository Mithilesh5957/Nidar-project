package com.nidar.gcs.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Detection {
    private String id;
    private String vehicleId;
    private String imageUrl; // Relative URL to access the image
    private double lat;
    private double lon;
    private double confidence;
    private boolean approved;
    private long timestamp;
}
