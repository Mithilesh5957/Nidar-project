package com.nidar.gcs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "telemetry")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Telemetry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double latitude;
    private Double longitude;
    private Double altitude;
    private Double speed;
    private Double battery;
    private Integer heading;
    private Integer satellites;
    private String flightMode;
    private Boolean armed;
    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "drone_id")
    private Drone drone;

    @ManyToOne
    @JoinColumn(name = "mission_id")
    private Mission mission;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
