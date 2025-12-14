package com.nidar.gcs.controller;

import com.nidar.gcs.model.Telemetry;
import com.nidar.gcs.service.MAVProxyService;
import com.nidar.gcs.service.TelemetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drone")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:3000" })
public class DroneController {

    private final MAVProxyService mavProxyService;
    private final TelemetryService telemetryService;

    @PostMapping("/connect")
    public ResponseEntity<Map<String, Object>> connect() {
        log.info("Connecting to drone via MAVProxy");
        boolean success = mavProxyService.connect();

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "Connected successfully" : "Connection failed");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/disconnect")
    public ResponseEntity<Map<String, Object>> disconnect() {
        log.info("Disconnecting from drone");
        mavProxyService.disconnect();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Disconnected successfully");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("connected", mavProxyService.isConnected());

        Telemetry latest = telemetryService.getLatestTelemetry();
        if (latest != null) {
            response.put("telemetry", latest);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/telemetry")
    public ResponseEntity<List<Telemetry>> getTelemetry() {
        return ResponseEntity.ok(telemetryService.getRecentTelemetry());
    }

    @GetMapping("/telemetry/latest")
    public ResponseEntity<Telemetry> getLatestTelemetry() {
        Telemetry telemetry = telemetryService.getLatestTelemetry();
        if (telemetry != null) {
            return ResponseEntity.ok(telemetry);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/command/{command}")
    public ResponseEntity<Map<String, Object>> sendCommand(@PathVariable String command) {
        log.info("Sending command: {}", command);
        boolean success = mavProxyService.sendCommand(command);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "Command sent successfully" : "Failed to send command");

        return ResponseEntity.ok(response);
    }
}
