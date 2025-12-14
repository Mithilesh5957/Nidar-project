package com.nidar.gcs.service;

import com.nidar.gcs.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Service for creating and sending MAVLink messages to Mission Planner/QGC.
 * Uses manual MAVLink v1 packet serialization with proper CRC-16 calculation.
 */
@Service
@Slf4j
public class MAVLinkMessageService {

    private static final int MAVLINK_STX = 0xFE; // MAVLink v1 start byte
    private byte sequenceNumber = 0;

    // CRC-16/MCRF4XX lookup table (used by MAVLink)
    private static final int[] CRC_TABLE = new int[256];
    static {
        for (int i = 0; i < 256; i++) {
            int crc = i;
            for (int j = 0; j < 8; j++) {
                if ((crc & 1) != 0) {
                    crc = (crc >>> 1) ^ 0x8408;
                } else {
                    crc = crc >>> 1;
                }
            }
            CRC_TABLE[i] = crc;
        }
    }

    // CRC Extra bytes for each message type (from MAVLink specification)
    private static final int CRC_EXTRA_HEARTBEAT = 50;
    private static final int CRC_EXTRA_GLOBAL_POSITION_INT = 104;
    private static final int CRC_EXTRA_MISSION_COUNT = 221;
    private static final int CRC_EXTRA_MISSION_ITEM_INT = 38;
    private static final int CRC_EXTRA_PARAM_SET = 168;
    private static final int CRC_EXTRA_COMMAND_LONG = 152;
    private static final int CRC_EXTRA_FENCE_POINT = 78;
    private static final int CRC_EXTRA_RALLY_POINT = 138;

    private int crc16Accumulate(int data, int crc) {
        int tmp = (data ^ crc) & 0xFF;
        return (crc >>> 8) ^ CRC_TABLE[tmp];
    }

    private int calculateCrc(byte[] data, int crcExtra) {
        int crc = 0xFFFF;
        for (byte b : data) {
            crc = crc16Accumulate(b & 0xFF, crc);
        }
        // Include CRC extra byte
        crc = crc16Accumulate(crcExtra, crc);
        return crc & 0xFFFF;
    }

    private boolean sendPacket(DatagramSocket socket, InetAddress address, int port,
            int msgId, byte[] payload, int crcExtra) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            // MAVLink v1 header
            out.write(MAVLINK_STX);
            out.write(payload.length);
            out.write(sequenceNumber++);
            out.write(1); // System ID
            out.write(1); // Component ID
            out.write(msgId);

            // Payload
            out.write(payload);

            // Calculate CRC over header (excluding STX) + payload
            byte[] forCrc = new byte[5 + payload.length];
            forCrc[0] = (byte) payload.length;
            forCrc[1] = (byte) (sequenceNumber - 1);
            forCrc[2] = 1; // System ID
            forCrc[3] = 1; // Component ID
            forCrc[4] = (byte) msgId;
            System.arraycopy(payload, 0, forCrc, 5, payload.length);

            int crc = calculateCrc(forCrc, crcExtra);
            out.write(crc & 0xFF);
            out.write((crc >> 8) & 0xFF);

            byte[] packet = out.toByteArray();
            DatagramPacket datagram = new DatagramPacket(packet, packet.length, address, port);
            socket.send(datagram);
            return true;
        } catch (IOException e) {
            log.error("Failed to send MAVLink packet", e);
            return false;
        }
    }

    /**
     * Send HEARTBEAT message (ID 0)
     */
    public boolean sendHeartbeat(DatagramSocket socket, InetAddress address, int port) {
        ByteBuffer buf = ByteBuffer.allocate(9).order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(0); // custom_mode (4 bytes)
        buf.put((byte) 2); // type: MAV_TYPE_QUADROTOR
        buf.put((byte) 3); // autopilot: MAV_AUTOPILOT_ARDUPILOTMEGA
        buf.put((byte) 0x81); // base_mode: CUSTOM_MODE_ENABLED | SAFETY_ARMED
        buf.put((byte) 4); // system_status: MAV_STATE_ACTIVE
        buf.put((byte) 3); // mavlink_version

        return sendPacket(socket, address, port, 0, buf.array(), CRC_EXTRA_HEARTBEAT);
    }

    /**
     * Send GLOBAL_POSITION_INT message (ID 33)
     */
    public boolean sendGlobalPositionInt(DatagramSocket socket, InetAddress address, int port, Telemetry telemetry) {
        ByteBuffer buf = ByteBuffer.allocate(28).order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt((int) (System.currentTimeMillis() % Integer.MAX_VALUE)); // time_boot_ms
        buf.putInt((int) (telemetry.getLatitude() * 1e7)); // lat
        buf.putInt((int) (telemetry.getLongitude() * 1e7)); // lon
        buf.putInt((int) (telemetry.getAltitude() * 1000)); // alt (mm)
        buf.putInt((int) (telemetry.getAltitude() * 1000)); // relative_alt (mm)
        buf.putShort((short) (telemetry.getSpeed() * 100)); // vx (cm/s)
        buf.putShort((short) 0); // vy
        buf.putShort((short) 0); // vz
        buf.putShort((short) (telemetry.getHeading() * 100)); // hdg (cdeg)

        return sendPacket(socket, address, port, 33, buf.array(), CRC_EXTRA_GLOBAL_POSITION_INT);
    }

    /**
     * Send MISSION_COUNT message (ID 44)
     */
    public boolean sendMissionCount(DatagramSocket socket, InetAddress address, int port, int count) {
        ByteBuffer buf = ByteBuffer.allocate(5).order(ByteOrder.LITTLE_ENDIAN);
        buf.putShort((short) count); // count
        buf.put((byte) 0); // target_system
        buf.put((byte) 0); // target_component
        buf.put((byte) 0); // mission_type: MAV_MISSION_TYPE_MISSION

        log.info("Sent MISSION_COUNT: {} items", count);
        return sendPacket(socket, address, port, 44, buf.array(), CRC_EXTRA_MISSION_COUNT);
    }

    /**
     * Send MISSION_ITEM_INT message (ID 73)
     */
    public boolean sendMissionItem(DatagramSocket socket, InetAddress address, int port,
            Waypoint waypoint, int sequence) {
        ByteBuffer buf = ByteBuffer.allocate(38).order(ByteOrder.LITTLE_ENDIAN);
        buf.putFloat(waypoint.getSpeed() != null ? waypoint.getSpeed().floatValue() : 0f); // param1
        buf.putFloat(waypoint.getAcceptanceRadius() != null ? waypoint.getAcceptanceRadius().floatValue() : 0f); // param2
        buf.putFloat(waypoint.getPassRadius() != null ? waypoint.getPassRadius().floatValue() : 0f); // param3
        buf.putFloat(waypoint.getYaw() != null ? waypoint.getYaw().floatValue() : 0f); // param4
        buf.putInt((int) (waypoint.getLatitude() * 1e7)); // x (lat)
        buf.putInt((int) (waypoint.getLongitude() * 1e7)); // y (lon)
        buf.putFloat(waypoint.getAltitude().floatValue()); // z (alt)
        buf.putShort((short) sequence); // seq
        buf.putShort((short) getCommandId(waypoint.getCommand())); // command
        buf.put((byte) 0); // target_system
        buf.put((byte) 0); // target_component
        buf.put((byte) 6); // frame: MAV_FRAME_GLOBAL_RELATIVE_ALT_INT
        buf.put((byte) (sequence == 0 ? 1 : 0)); // current
        buf.put((byte) 1); // autocontinue
        buf.put((byte) 0); // mission_type

        log.info("Sent MISSION_ITEM_INT {}: {} at {},{},{}",
                sequence, waypoint.getCommand(), waypoint.getLatitude(),
                waypoint.getLongitude(), waypoint.getAltitude());
        return sendPacket(socket, address, port, 73, buf.array(), CRC_EXTRA_MISSION_ITEM_INT);
    }

    /**
     * Send PARAM_SET message (ID 23)
     */
    public boolean sendParameter(DatagramSocket socket, InetAddress address, int port,
            String paramName, float paramValue) {
        ByteBuffer buf = ByteBuffer.allocate(23).order(ByteOrder.LITTLE_ENDIAN);
        buf.putFloat(paramValue); // param_value
        buf.put((byte) 0); // target_system
        buf.put((byte) 0); // target_component

        // param_id (16 chars, null-padded)
        byte[] paramBytes = new byte[16];
        byte[] nameBytes = paramName.getBytes();
        System.arraycopy(nameBytes, 0, paramBytes, 0, Math.min(nameBytes.length, 16));
        buf.put(paramBytes);

        buf.put((byte) 9); // param_type: MAV_PARAM_TYPE_REAL32

        log.info("Sent PARAM_SET: {}={}", paramName, paramValue);
        return sendPacket(socket, address, port, 23, buf.array(), CRC_EXTRA_PARAM_SET);
    }

    /**
     * Send FENCE_POINT message (ID 160)
     */
    public boolean sendGeofencePoint(DatagramSocket socket, InetAddress address, int port,
            GeofencePoint point, int sequence, int totalPoints) {
        ByteBuffer buf = ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN);
        buf.putFloat(point.getLatitude().floatValue()); // lat
        buf.putFloat(point.getLongitude().floatValue()); // lng
        buf.put((byte) 0); // target_system
        buf.put((byte) 0); // target_component
        buf.put((byte) sequence); // idx
        buf.put((byte) totalPoints); // count

        log.info("Sent FENCE_POINT {}/{}", sequence, totalPoints);
        return sendPacket(socket, address, port, 160, buf.array(), CRC_EXTRA_FENCE_POINT);
    }

    /**
     * Send RALLY_POINT message (ID 175)
     */
    public boolean sendRallyPoint(DatagramSocket socket, InetAddress address, int port,
            RallyPoint point, int sequence, int totalPoints) {
        ByteBuffer buf = ByteBuffer.allocate(19).order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt((int) (point.getLatitude() * 1e7)); // lat
        buf.putInt((int) (point.getLongitude() * 1e7)); // lng
        buf.putShort(point.getAltitude().shortValue()); // alt
        buf.putShort(point.getBreakAltitude() != null ? point.getBreakAltitude().shortValue() : 0); // break_alt
        buf.putShort(point.getLandDirection() != null ? point.getLandDirection().shortValue() : 0); // land_dir
        buf.put((byte) 0); // target_system
        buf.put((byte) 0); // target_component
        buf.put((byte) sequence); // idx
        buf.put((byte) totalPoints); // count
        buf.put((byte) 0); // flags

        log.info("Sent RALLY_POINT {}/{}", sequence, totalPoints);
        return sendPacket(socket, address, port, 175, buf.array(), CRC_EXTRA_RALLY_POINT);
    }

    /**
     * Send COMMAND_LONG message (ID 76)
     */
    public boolean sendCommand(DatagramSocket socket, InetAddress address, int port,
            int command, float param1, float param2, float param3,
            float param4, float param5, float param6, float param7) {
        ByteBuffer buf = ByteBuffer.allocate(33).order(ByteOrder.LITTLE_ENDIAN);
        buf.putFloat(param1);
        buf.putFloat(param2);
        buf.putFloat(param3);
        buf.putFloat(param4);
        buf.putFloat(param5);
        buf.putFloat(param6);
        buf.putFloat(param7);
        buf.putShort((short) command);
        buf.put((byte) 0); // target_system
        buf.put((byte) 0); // target_component
        buf.put((byte) 0); // confirmation

        log.info("Sent COMMAND_LONG: command={}", command);
        return sendPacket(socket, address, port, 76, buf.array(), CRC_EXTRA_COMMAND_LONG);
    }

    private int getCommandId(String command) {
        if (command == null)
            return 16; // MAV_CMD_NAV_WAYPOINT

        switch (command.toUpperCase()) {
            case "WAYPOINT":
                return 16;
            case "TAKEOFF":
                return 22;
            case "LAND":
                return 21;
            case "LOITER_UNLIMITED":
                return 17;
            case "LOITER_TIME":
                return 19;
            case "RTL":
            case "RETURN_TO_LAUNCH":
                return 20;
            default:
                return 16;
        }
    }
}
