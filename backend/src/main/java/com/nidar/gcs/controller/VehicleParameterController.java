package com.nidar.gcs.controller;

import com.nidar.gcs.model.VehicleParameter;
import com.nidar.gcs.repository.VehicleParameterRepository;
import com.nidar.gcs.service.MAVProxyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/parameters")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:3000" })
public class VehicleParameterController {

    private final VehicleParameterRepository parameterRepository;
    private final MAVProxyService mavProxyService;

    @GetMapping
    public ResponseEntity<List<VehicleParameter>> getAllParameters() {
        return ResponseEntity.ok(parameterRepository.findAll());
    }

    @GetMapping("/{name}")
    public ResponseEntity<VehicleParameter> getParameter(@PathVariable String name) {
        return parameterRepository.findByParameterName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<VehicleParameter> createParameter(@RequestBody @NonNull VehicleParameter parameter) {
        VehicleParameter saved = parameterRepository.save(parameter);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{name}")
    @SuppressWarnings("null")
    public ResponseEntity<VehicleParameter> updateParameter(
            @PathVariable String name,
            @RequestBody Map<String, String> updates) {

        return parameterRepository.findByParameterName(name)
                .map(param -> {
                    if (updates.containsKey("value")) {
                        param.setParameterValue(updates.get("value"));
                    }
                    if (updates.containsKey("description")) {
                        param.setDescription(updates.get("description"));
                    }
                    VehicleParameter updated = parameterRepository.save(param);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadParameters(
            @RequestBody List<VehicleParameter> parameters) {

        log.info("Uploading {} vehicle parameters to Mission Planner/QGC", parameters.size());
        Map<String, Object> response = new HashMap<>();

        try {
            // Save to database
            List<VehicleParameter> saved = parameterRepository.saveAll(parameters);

            // Send to Mission Planner/QGC via MAVLink
            boolean success = mavProxyService.uploadParameters(saved);

            if (success) {
                response.put("success", true);
                response.put("message", "Parameters uploaded successfully to Mission Planner/QGC");
                response.put("count", saved.size());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to upload parameters to Mission Planner/QGC");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error uploading parameters", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteParameter(@PathVariable @NonNull Long id) {
        parameterRepository.deleteById(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Parameter deleted");

        return ResponseEntity.ok(response);
    }
}
