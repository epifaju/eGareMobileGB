package com.garemobilegb.vehicle.repository;

import com.garemobilegb.vehicle.dto.VehicleStatusCountDto;
import com.garemobilegb.vehicle.domain.Vehicle;
import com.garemobilegb.vehicle.domain.VehicleStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface VehicleRepository
    extends JpaRepository<Vehicle, Long>, JpaSpecificationExecutor<Vehicle> {

  interface StationActiveVehicleAggregate {
    long getStationId();

    long getActiveVehicles();

    java.time.Instant getNextDepartureAt();

    Integer getMinFareXof();
  }

  Page<Vehicle> findByStation_Id(long stationId, Pageable pageable);

  List<Vehicle> findAllByStation_Id(long stationId);

  Page<Vehicle> findByStation_IdAndArchivedFalse(long stationId, Pageable pageable);

  Page<Vehicle> findByStation_IdAndStatusNot(
      long stationId, VehicleStatus excluded, Pageable pageable);

  Page<Vehicle> findByStation_IdAndStatusNotAndArchivedFalse(
      long stationId, VehicleStatus excluded, Pageable pageable);

  Page<Vehicle> findByArchivedFalse(Pageable pageable);

  @Query(
      "SELECT DISTINCT v.routeLabel FROM Vehicle v WHERE v.archived = false AND LOWER(v.routeLabel) LIKE LOWER(CONCAT('%', :q, '%')) ORDER BY v.routeLabel ASC")
  Page<String> findDistinctRouteLabelsContaining(@Param("q") String q, Pageable pageable);

  List<Vehicle> findByStatusNotAndCurrentLatitudeIsNotNullAndCurrentLongitudeIsNotNullAndArchivedFalse(
      VehicleStatus status);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT v FROM Vehicle v WHERE v.id = :id")
  Optional<Vehicle> findByIdForUpdate(@Param("id") long id);

  @Query(
      "SELECT v.station.id AS stationId, COUNT(v.id) AS activeVehicles, "
          + "MIN(v.departureScheduledAt) AS nextDepartureAt, MIN(v.fareAmountXof) AS minFareXof "
          + "FROM Vehicle v WHERE v.station.id IN :stationIds AND v.status <> :excludedStatus "
          + "AND v.archived = false "
          + "GROUP BY v.station.id")
  List<StationActiveVehicleAggregate> aggregateActiveByStationIds(
      @Param("stationIds") List<Long> stationIds, @Param("excludedStatus") VehicleStatus excludedStatus);

  long countByArchivedFalse();

  long countByArchivedTrue();

  @Query(
      "SELECT new com.garemobilegb.vehicle.dto.VehicleStatusCountDto(v.status, COUNT(v)) FROM Vehicle v WHERE v.archived = false GROUP BY v.status")
  List<VehicleStatusCountDto> countActiveVehiclesGroupedByStatus();
}
