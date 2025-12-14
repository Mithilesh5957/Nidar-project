package com.nidar.gcs.repository;

import com.nidar.gcs.model.MissionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissionItemRepository extends JpaRepository<MissionItem, Long> {
    List<MissionItem> findByVehicleId(String vehicleId);

    List<MissionItem> findByVehicleIdOrderBySeqAsc(String vehicleId);

    void deleteByVehicleId(String vehicleId);
}
