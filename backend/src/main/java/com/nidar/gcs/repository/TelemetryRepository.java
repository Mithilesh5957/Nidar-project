package com.nidar.gcs.repository;

import com.nidar.gcs.model.TelemetryPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TelemetryRepository extends JpaRepository<TelemetryPoint, Long> {
    List<TelemetryPoint> findByVehicleId(String vehicleId);
}
