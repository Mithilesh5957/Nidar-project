package com.nidar.gcs.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MissionItem {
    private int seq;
    private String command; // TAKEOFF, WAYPOINT, LAND, DO_SET_SERVO (drop)
    private double lat;
    private double lon;
    private double alt;
    private double param1; // e.g., hold time
    private double param2;
    private double param3;
    private double param4;
}
