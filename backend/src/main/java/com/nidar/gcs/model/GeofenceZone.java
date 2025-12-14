package com.nidar.gcs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "geofence_zones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeofenceZone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String zoneType; // INCLUSION, EXCLUSION
    private Boolean enabled;

    // Altitude constraints
    private Double minAltitude;
    private Double maxAltitude;

    // Action when violated
    private String violationAction; // WARN, RTL, LAND, BRAKE

    @ManyToOne
    @JoinColumn(name = "mission_id")
    private Mission mission;

    @OneToMany(mappedBy = "zone", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GeofencePoint> points = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (enabled == null) {
            enabled = true;
        }
        if (violationAction == null) {
            violationAction = "WARN";
        }
    }
}
