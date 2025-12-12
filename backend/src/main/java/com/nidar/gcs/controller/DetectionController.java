package com.nidar.gcs.controller;

import com.nidar.gcs.model.Detection;
import com.nidar.gcs.model.MissionItem;
import com.nidar.gcs.service.MissionService;
import com.nidar.gcs.service.StorageService;
import com.nidar.gcs.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class DetectionController {

    @Autowired
    private StorageService storageService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private MissionService missionService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping("/upload_detection/{vehicle}")
    public Detection uploadDetection(@PathVariable String vehicle,
            @RequestParam("file") MultipartFile file,
            @RequestParam("lat") double lat,
            @RequestParam("lon") double lon,
            @RequestParam("confidence") double confidence) {

        String filename = storageService.store(file);
        if (filename == null) filename = "unknown";

        String path = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(filename)
                .toUriString();
        
        // Ensure path is not null (though toUriString shouldn't be null)
        String fileDownloadUri = (path != null) ? path : "";

        Detection detection = new Detection(
                UUID.randomUUID().toString(),
                vehicle,
                fileDownloadUri,
                lat,
                lon,
                confidence,
                false,
                System.currentTimeMillis());

        vehicleService.addDetection(detection);

        // Broadcast
        messagingTemplate.convertAndSend("/topic/detections", detection);

        return detection;
    }

    @GetMapping("/detections")
    public List<Detection> getDetections() {
        return vehicleService.getDetections();
    }

    @PostMapping("/detections/{id}/approve")
    public void approveDetection(@PathVariable String id) {
        Detection d = vehicleService.getDetection(id);
        if (d != null) {
            d.setApproved(true);

            // Generate Mission for Delivery Drone
            missionService.generateMissionForDetection(d, "delivery");
            List<MissionItem> newMission = missionService.getMission("delivery");
            if (newMission != null) {
                // Broadcast Mission Update
                messagingTemplate.convertAndSend("/topic/missions/delivery", newMission);
            }

            // Broadcast Detection Update (Approved status)
            messagingTemplate.convertAndSend("/topic/detections", d);
        }
    }
}
