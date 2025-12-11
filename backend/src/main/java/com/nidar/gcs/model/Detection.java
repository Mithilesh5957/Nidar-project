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
@Table(name = "detections")
public class Detection {
    @Id
    private String id;
    private String vehicleId;
    private String imageUrl; // Relative URL to access the image
    private double lat;
    private double lon;
    private double confidence;
    private boolean approved;
    private long timestamp;
}
