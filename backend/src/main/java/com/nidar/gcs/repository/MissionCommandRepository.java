package com.nidar.gcs.repository;

import com.nidar.gcs.model.MissionCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionCommandRepository extends JpaRepository<MissionCommand, Long> {
}
