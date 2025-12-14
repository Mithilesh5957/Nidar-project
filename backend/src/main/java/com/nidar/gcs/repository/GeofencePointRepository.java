package com.nidar.gcs.repository;

import com.nidar.gcs.model.GeofencePoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GeofencePointRepository extends JpaRepository<GeofencePoint, Long> {
}
