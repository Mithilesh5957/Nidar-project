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
    private int frame;
    private int command;
    private int current;
    private int autocontinue;
    
    private float param1;
    private float param2;
    private float param3;
    private float param4;
    
    // MAVLink uses x/y/z for generic coordinates. 
    // For GLOBAL frames, x=lat, y=lon, z=alt.
    private double x;
    private double y;
    private float z;
}
