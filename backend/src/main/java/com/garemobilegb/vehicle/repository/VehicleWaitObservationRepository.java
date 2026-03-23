package com.garemobilegb.vehicle.repository;

import com.garemobilegb.vehicle.domain.VehicleStatus;
import com.garemobilegb.vehicle.domain.VehicleWaitObservation;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehicleWaitObservationRepository extends JpaRepository<VehicleWaitObservation, Long> {

  @Query(
      "SELECT COUNT(o), AVG(o.observedWaitMinutes) FROM VehicleWaitObservation o "
          + "WHERE o.observedAt >= :since AND o.stationId = :stationId AND o.status = :status "
          + "AND o.hourBucket = :hourBucket AND o.dayOfWeek = :dayOfWeek AND o.fillBucket = :fillBucket")
  Object[] aggregateStrict(
      @Param("since") Instant since,
      @Param("stationId") long stationId,
      @Param("status") VehicleStatus status,
      @Param("hourBucket") int hourBucket,
      @Param("dayOfWeek") int dayOfWeek,
      @Param("fillBucket") int fillBucket);

  @Query(
      "SELECT COUNT(o), AVG(o.observedWaitMinutes) FROM VehicleWaitObservation o "
          + "WHERE o.observedAt >= :since AND o.stationId = :stationId AND o.status = :status "
          + "AND o.hourBucket = :hourBucket")
  Object[] aggregateStationHour(
      @Param("since") Instant since,
      @Param("stationId") long stationId,
      @Param("status") VehicleStatus status,
      @Param("hourBucket") int hourBucket);

  @Query(
      "SELECT COUNT(o), AVG(o.observedWaitMinutes) FROM VehicleWaitObservation o "
          + "WHERE o.observedAt >= :since AND o.stationId = :stationId AND o.status = :status")
  Object[] aggregateStation(
      @Param("since") Instant since, @Param("stationId") long stationId, @Param("status") VehicleStatus status);

  @Query(
      "SELECT COUNT(o), AVG(o.observedWaitMinutes) FROM VehicleWaitObservation o "
          + "WHERE o.observedAt >= :since AND o.status = :status AND o.hourBucket = :hourBucket")
  Object[] aggregateGlobalHour(
      @Param("since") Instant since, @Param("status") VehicleStatus status, @Param("hourBucket") int hourBucket);
}
