package com.nidar.gcs.repository;

import com.nidar.gcs.model.VehicleParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleParameterRepository extends JpaRepository<VehicleParameter, Long> {
    Optional<VehicleParameter> findByParameterName(String parameterName);
}
