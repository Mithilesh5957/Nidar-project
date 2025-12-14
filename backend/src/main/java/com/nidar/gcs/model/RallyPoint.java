package com.nidar.gcs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rally_points")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RallyPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Double latitude;
    private Double longitude;
    private Double altitude;
    private Integer breakAltitude; // Altitude to break to when returning
    private Integer landDirection; // Direction to land (degrees)

    @ManyToOne
    @JoinColumn(name = "mission_id")
    private Mission mission;
}
