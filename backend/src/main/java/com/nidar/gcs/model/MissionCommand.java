package com.nidar.gcs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mission_commands")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MissionCommand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer sequence;
    private String commandType; // TAKEOFF, LAND, LOITER, CHANGE_SPEED, etc.
    private Double param1; // Command-specific parameter 1
    private Double param2; // Command-specific parameter 2
    private Double param3; // Command-specific parameter 3
    private Double param4; // Command-specific parameter 4
    private Double latitude;
    private Double longitude;
    private Double altitude;
    private Integer autocontinue; // 0 or 1

    @ManyToOne
    @JoinColumn(name = "mission_id")
    private Mission mission;

    // Common command types
    public static final String WAYPOINT = "WAYPOINT";
    public static final String TAKEOFF = "TAKEOFF";
    public static final String LAND = "LAND";
    public static final String LOITER_UNLIMITED = "LOITER_UNLIMITED";
    public static final String LOITER_TIME = "LOITER_TIME";
    public static final String LOITER_TURNS = "LOITER_TURNS";
    public static final String RETURN_TO_LAUNCH = "RTL";
    public static final String CHANGE_SPEED = "CHANGE_SPEED";
    public static final String DO_SET_SERVO = "DO_SET_SERVO";
    public static final String DO_SET_CAM_TRIGG_DIST = "DO_SET_CAM_TRIGG_DIST";
    public static final String DO_DIGICAM_CONTROL = "DO_DIGICAM_CONTROL";
    public static final String CONDITION_DELAY = "CONDITION_DELAY";
    public static final String CONDITION_DISTANCE = "CONDITION_DISTANCE";
}
