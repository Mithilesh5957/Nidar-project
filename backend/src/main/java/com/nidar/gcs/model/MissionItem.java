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
@Table(name = "mission_items")
public class MissionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String vehicleId;
    private int seq;
    private String command;
    private double lat;
    private double lon;
    private double alt;
    private double p1;
    private double p2;
    private double p3;
    private double p4;

    public MissionItem(int seq, String command, double lat, double lon, double alt, double p1, double p2, double p3,
            double p4) {
        this.seq = seq;
        this.command = command;
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
        this.p4 = p4;
    }
}
