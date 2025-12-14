package com.nidar.gcs.controller;

import com.nidar.gcs.model.Telemetry;
import com.nidar.gcs.service.MissionReplayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/replay")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:3000" })
public class MissionReplayController {

    private final MissionReplayService missionReplayService;

    @GetMapping("/mission/{missionId}")
    public ResponseEntity<Map<String, Object>> getMissionReplayData(@PathVariable @NonNull Long missionId) {
        log.info("Getting replay data for mission: {}", missionId);
        Map<String, Object> data = missionReplayService.getMissionReplayData(missionId);

        if (data.containsKey("error")) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(data);
    }

    @GetMapping("/telemetry/{missionId}")
    public ResponseEntity<List<Telemetry>> getTelemetryByMission(@PathVariable @NonNull Long missionId) {
        List<Telemetry> telemetry = missionReplayService.getTelemetryByMission(missionId);
        return ResponseEntity.ok(telemetry);
    }
}
