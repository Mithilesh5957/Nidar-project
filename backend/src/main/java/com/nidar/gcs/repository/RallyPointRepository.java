package com.nidar.gcs.repository;

import com.nidar.gcs.model.RallyPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RallyPointRepository extends JpaRepository<RallyPoint, Long> {
}
