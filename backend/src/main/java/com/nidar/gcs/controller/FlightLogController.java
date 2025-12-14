package com.nidar.gcs.controller;

import com.nidar.gcs.model.FlightLog;
import com.nidar.gcs.repository.FlightLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:3000" })
public class FlightLogController {

    private final FlightLogRepository flightLogRepository;

    @GetMapping
    public ResponseEntity<List<FlightLog>> getAllLogs() {
        return ResponseEntity.ok(flightLogRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlightLog> getLogById(@PathVariable @NonNull Long id) {
        return flightLogRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<FlightLog> createLog(@RequestBody @NonNull FlightLog log) {
        FlightLog saved = flightLogRepository.save(log);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FlightLog> updateLog(@PathVariable @NonNull Long id, @RequestBody @NonNull FlightLog log) {
        return flightLogRepository.findById(id)
                .map(existing -> {
                    log.setId(id);
                    FlightLog updated = java.util.Objects.requireNonNull(flightLogRepository.save(log));
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteLog(@PathVariable @NonNull Long id) {
        flightLogRepository.deleteById(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Flight log deleted");

        return ResponseEntity.ok(response);
    }
}
