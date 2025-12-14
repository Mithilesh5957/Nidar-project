package com.nidar.gcs.repository;

import com.nidar.gcs.model.GeofenceZone;
import com.nidar.gcs.model.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeofenceZoneRepository extends JpaRepository<GeofenceZone, Long> {
    List<GeofenceZone> findByMission(Mission mission);

    List<GeofenceZone> findByMissionAndEnabled(Mission mission, Boolean enabled);
}
