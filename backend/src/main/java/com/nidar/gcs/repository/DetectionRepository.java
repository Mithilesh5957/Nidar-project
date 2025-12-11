package com.nidar.gcs.repository;

import com.nidar.gcs.model.Detection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetectionRepository extends JpaRepository<Detection, String> {
}
