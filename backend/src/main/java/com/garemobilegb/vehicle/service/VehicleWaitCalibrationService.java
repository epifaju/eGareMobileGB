package com.garemobilegb.vehicle.service;

import com.garemobilegb.vehicle.domain.Vehicle;
import com.garemobilegb.vehicle.domain.VehicleStatus;
import com.garemobilegb.vehicle.domain.VehicleWaitObservation;
import com.garemobilegb.vehicle.dto.VehicleResponse;
import com.garemobilegb.vehicle.repository.VehicleWaitObservationRepository;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehicleWaitCalibrationService {

  private static final int LOOKBACK_DAYS = 21;

  private final VehicleWaitObservationRepository repository;

  public VehicleWaitCalibrationService(VehicleWaitObservationRepository repository) {
    this.repository = repository;
  }

  @Transactional
  public void recordObservation(VehicleResponse v, Instant now) {
    if (v.departureScheduledAt() == null || v.status() == VehicleStatus.PARTI) {
      return;
    }
    long mins = Duration.between(now, v.departureScheduledAt()).toMinutes();
    if (mins < 0 || mins > 180) {
      return;
    }
    int fillBucket = fillBucket(v.occupiedSeats(), v.capacity());
    int hour = now.atOffset(ZoneOffset.UTC).getHour();
    DayOfWeek dow = now.atOffset(ZoneOffset.UTC).getDayOfWeek();
    repository.save(
        new VehicleWaitObservation(
            v.stationId(), v.status(), fillBucket, hour, dow.getValue(), (int) mins, now));
  }

  @Transactional(readOnly = true)
  public Optional<CalibrationPoint> lookup(Vehicle v, Instant now) {
    Instant since = now.minus(Duration.ofDays(LOOKBACK_DAYS));
    int hour = now.atOffset(ZoneOffset.UTC).getHour();
    int dayOfWeek = now.atOffset(ZoneOffset.UTC).getDayOfWeek().getValue();
    int fillBucket = fillBucket(v.getOccupiedSeats(), v.getCapacity());

    CalibrationPoint strict =
        toPoint(repository.aggregateStrict(since, v.getStation().getId(), v.getStatus(), hour, dayOfWeek, fillBucket));
    if (strict.samples() >= 4) {
      return Optional.of(strict);
    }
    CalibrationPoint stationHour =
        toPoint(repository.aggregateStationHour(since, v.getStation().getId(), v.getStatus(), hour));
    if (stationHour.samples() >= 6) {
      return Optional.of(stationHour);
    }
    CalibrationPoint station =
        toPoint(repository.aggregateStation(since, v.getStation().getId(), v.getStatus()));
    if (station.samples() >= 8) {
      return Optional.of(station);
    }
    CalibrationPoint globalHour = toPoint(repository.aggregateGlobalHour(since, v.getStatus(), hour));
    if (globalHour.samples() >= 10) {
      return Optional.of(globalHour);
    }
    return Optional.empty();
  }

  private static CalibrationPoint toPoint(Object[] raw) {
    long samples = raw[0] == null ? 0L : ((Number) raw[0]).longValue();
    double avg = raw[1] == null ? 0.0d : ((Number) raw[1]).doubleValue();
    return new CalibrationPoint(samples, avg);
  }

  private static int fillBucket(int occupied, int capacity) {
    int cap = Math.max(1, capacity);
    double ratio = (double) Math.max(0, Math.min(occupied, cap)) / (double) cap;
    if (ratio < 0.2) {
      return 0;
    }
    if (ratio < 0.4) {
      return 1;
    }
    if (ratio < 0.6) {
      return 2;
    }
    if (ratio < 0.8) {
      return 3;
    }
    return 4;
  }

  public record CalibrationPoint(long samples, double avgMinutes) {}
}
