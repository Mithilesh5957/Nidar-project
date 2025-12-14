package com.nidar.gcs.controller;

import com.nidar.gcs.model.Mission;
import com.nidar.gcs.service.MissionService;
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
}
