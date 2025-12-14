package com.nidar.gcs.service;

import com.nidar.gcs.model.Telemetry;
import com.nidar.gcs.repository.TelemetryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TelemetryService {

    private final TelemetryRepository telemetryRepository;

    public Telemetry saveTelemetry(@NonNull Telemetry telemetry) {
        return telemetryRepository.save(telemetry);
    }

    public List<Telemetry> getRecentTelemetry() {
        return telemetryRepository.findTop100ByOrderByTimestampDesc();
    }

    public Telemetry getLatestTelemetry() {
        List<Telemetry> telemetryList = telemetryRepository.findTop100ByOrderByTimestampDesc();
        return telemetryList.isEmpty() ? null : telemetryList.get(0);
    }
}
