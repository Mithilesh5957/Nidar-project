package com.nidar.gcs.controller;

import com.nidar.gcs.model.Mission;
import com.nidar.gcs.service.MissionService;
import com.nidar.gcs.service.MissionExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mission")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:3000" })
public class MissionController {

    private final MissionService missionService;
    private final MissionExecutionService missionExecutionService;

    @PostMapping
    public ResponseEntity<Mission> createMission(@RequestBody Mission mission) {
        log.info("Creating new mission: {}", mission.getName());
        Mission created = missionService.createMission(mission);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<List<Mission>> getAllMissions() {
        return ResponseEntity.ok(missionService.getAllMissions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mission> getMissionById(@PathVariable @NonNull Long id) {
        Mission mission = missionService.getMissionById(id);
        if (mission != null) {
            return ResponseEntity.ok(mission);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/deploy")
    public ResponseEntity<Map<String, Object>> deployMission(@PathVariable @NonNull Long id) {
        log.info("Deploying mission: {}", id);
        Map<String, Object> response = new HashMap<>();

        try {
            Mission mission = missionService.deployMission(id);
            response.put("success", true);
            response.put("message", "Mission deployed successfully");
            response.put("mission", mission);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to deploy mission", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadMissionFile(@RequestParam("file") MultipartFile file) {
        log.info("Uploading mission file: {}", file.getOriginalFilename());
        Map<String, Object> response = new HashMap<>();

        try {
            Mission mission = missionService.parseMissionFile(file);
            response.put("success", true);
            response.put("message", "Mission file uploaded and parsed successfully");
            response.put("mission", mission);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to upload mission file", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<Map<String, Object>> executeMission(@PathVariable @NonNull Long id) {
        log.info("Executing mission: {}", id);
        Map<String, Object> response = new HashMap<>();

        try {
            Mission mission = missionService.getMissionById(id);
            if (mission == null) {
                response.put("success", false);
                response.put("message", "Mission not found");
                return ResponseEntity.notFound().build();
            }

            boolean started = missionExecutionService.startMission(mission);
            if (started) {
                response.put("success", true);
                response.put("message", "Mission execution started");
                response.put("status", missionExecutionService.getStatus());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to start mission");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Failed to execute mission", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<Map<String, Object>> pauseMission(@PathVariable @NonNull Long id) {
        log.info("Pausing mission: {}", id);
        Map<String, Object> response = new HashMap<>();

        try {
            missionExecutionService.pauseMission();
            response.put("success", true);
            response.put("message", "Mission paused");
            response.put("status", missionExecutionService.getStatus());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to pause mission", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<Map<String, Object>> resumeMission(@PathVariable @NonNull Long id) {
        log.info("Resuming mission: {}", id);
        Map<String, Object> response = new HashMap<>();

        try {
            missionExecutionService.resumeMission();
            response.put("success", true);
            response.put("message", "Mission resumed");
            response.put("status", missionExecutionService.getStatus());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to resume mission", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<Map<String, Object>> stopMission(@PathVariable @NonNull Long id) {
        log.info("Stopping mission: {}", id);
        Map<String, Object> response = new HashMap<>();

        try {
            missionExecutionService.stopMission();
            response.put("success", true);
            response.put("message", "Mission stopped");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to stop mission", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<MissionExecutionService.ExecutionStatus> getMissionStatus() {
        return ResponseEntity.ok(missionExecutionService.getStatus());
    }

    @PostMapping("/arm")
    public ResponseEntity<Map<String, Object>> armDrone() {
        log.info("Arming drone");
        Map<String, Object> response = new HashMap<>();

        try {
            boolean armed = missionExecutionService.arm();
            response.put("success", armed);
            response.put("message", armed ? "Drone armed" : "Drone already armed");
            response.put("status", missionExecutionService.getStatus());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to arm drone", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/disarm")
    public ResponseEntity<Map<String, Object>> disarmDrone() {
        log.info("Disarming drone");
        Map<String, Object> response = new HashMap<>();

        try {
            boolean disarmed = missionExecutionService.disarm();
            response.put("success", disarmed);
            response.put("message", disarmed ? "Drone disarmed" : "Cannot disarm - mission in progress");
            response.put("status", missionExecutionService.getStatus());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to disarm drone", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
