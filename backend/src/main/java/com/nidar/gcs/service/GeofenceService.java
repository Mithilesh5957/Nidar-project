package com.nidar.gcs.service;

import com.nidar.gcs.model.GeofencePoint;
import com.nidar.gcs.model.GeofenceZone;
import com.nidar.gcs.model.Mission;
import com.nidar.gcs.repository.GeofenceZoneRepository;
import com.nidar.gcs.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeofenceService {

    private final GeofenceZoneRepository geofenceZoneRepository;
    private final MissionRepository missionRepository;

    public List<GeofenceZone> getAllZones() {
        return geofenceZoneRepository.findAll();
    }

    public GeofenceZone getZoneById(@NonNull Long id) {
        return geofenceZoneRepository.findById(id).orElse(null);
    }

    public List<GeofenceZone> getZonesByMission(@NonNull Long missionId) {
        Mission mission = missionRepository.findById(missionId).orElse(null);
        if (mission != null) {
            return geofenceZoneRepository.findByMission(mission);
        }
        return List.of();
    }

    public GeofenceZone createZone(GeofenceZone zone) {
        log.info("Creating geofence zone: {}", zone.getName());
        return geofenceZoneRepository.save(zone);
    }

    public GeofenceZone updateZone(@NonNull Long id, GeofenceZone zoneDetails) {
        GeofenceZone zone = getZoneById(id);
        if (zone != null) {
            zone.setName(zoneDetails.getName());
            zone.setZoneType(zoneDetails.getZoneType());
            zone.setEnabled(zoneDetails.getEnabled());
            zone.setMinAltitude(zoneDetails.getMinAltitude());
            zone.setMaxAltitude(zoneDetails.getMaxAltitude());
            zone.setViolationAction(zoneDetails.getViolationAction());
            return geofenceZoneRepository.save(zone);
        }
        return null;
    }

    public void deleteZone(@NonNull Long id) {
        geofenceZoneRepository.deleteById(id);
    }

    public Map<String, Object> validatePosition(Double latitude, Double longitude, Double altitude,
            @NonNull Long missionId) {
        Map<String, Object> result = new HashMap<>();
        result.put("valid", true);

        Mission mission = missionRepository.findById(missionId).orElse(null);
        if (mission == null) {
            result.put("valid", false);
            result.put("message", "Mission not found");
            return result;
        }

        List<GeofenceZone> zones = geofenceZoneRepository.findByMissionAndEnabled(mission, true);

        for (GeofenceZone zone : zones) {
            // Check altitude constraints
            if (zone.getMinAltitude() != null && altitude < zone.getMinAltitude()) {
                result.put("valid", false);
                result.put("message", "Below minimum altitude for zone: " + zone.getName());
                result.put("violationAction", zone.getViolationAction());
                return result;
            }

            if (zone.getMaxAltitude() != null && altitude > zone.getMaxAltitude()) {
                result.put("valid", false);
                result.put("message", "Above maximum altitude for zone: " + zone.getName());
                result.put("violationAction", zone.getViolationAction());
                return result;
            }

            // Check if point is inside zone (simplified point-in-polygon check)
            if (isPointInZone(latitude, longitude, zone)) {
                if ("EXCLUSION".equals(zone.getZoneType())) {
                    result.put("valid", false);
                    result.put("message", "Inside exclusion zone: " + zone.getName());
                    result.put("violationAction", zone.getViolationAction());
                    return result;
                }
            } else {
                if ("INCLUSION".equals(zone.getZoneType())) {
                    result.put("valid", false);
                    result.put("message", "Outside inclusion zone: " + zone.getName());
                    result.put("violationAction", zone.getViolationAction());
                    return result;
                }
            }
        }

        result.put("message", "Position is valid");
        return result;
    }

    private boolean isPointInZone(Double latitude, Double longitude, GeofenceZone zone) {
        List<GeofencePoint> points = zone.getPoints();
        if (points.size() < 3) {
            return false; // Not a valid polygon
        }

        // Ray casting algorithm for point-in-polygon test
        boolean inside = false;
        int j = points.size() - 1;

        for (int i = 0; i < points.size(); i++) {
            GeofencePoint pi = points.get(i);
            GeofencePoint pj = points.get(j);

            if ((pi.getLongitude() > longitude) != (pj.getLongitude() > longitude) &&
                    (latitude < (pj.getLatitude() - pi.getLatitude()) * (longitude - pi.getLongitude()) /
                            (pj.getLongitude() - pi.getLongitude()) + pi.getLatitude())) {
                inside = !inside;
            }
            j = i;
        }

        return inside;
    }
}
