package com.garemobilegb.vehicle.service;

import com.garemobilegb.booking.domain.BookingStatus;
import com.garemobilegb.booking.repository.BookingRepository;
import com.garemobilegb.shared.exceptions.BusinessException;
import com.garemobilegb.station.repository.StationRepository;
import com.garemobilegb.vehicle.domain.Vehicle;
import com.garemobilegb.vehicle.domain.VehicleStatus;
import com.garemobilegb.vehicle.dto.UpdateVehicleLocationRequest;
import com.garemobilegb.vehicle.dto.UpdateVehicleStatusRequest;
import com.garemobilegb.vehicle.dto.VehicleResponse;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.garemobilegb.vehicle.repository.VehicleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehicleService {

  private final VehicleRepository vehicleRepository;
  private final BookingRepository bookingRepository;
  private final StationRepository stationRepository;
  private final VehicleRealtimeService vehicleRealtimeService;
  private final VehicleWaitTimeEstimator vehicleWaitTimeEstimator;

  public VehicleService(
      VehicleRepository vehicleRepository,
      BookingRepository bookingRepository,
      StationRepository stationRepository,
      VehicleRealtimeService vehicleRealtimeService,
      VehicleWaitTimeEstimator vehicleWaitTimeEstimator) {
    this.vehicleRepository = vehicleRepository;
    this.bookingRepository = bookingRepository;
    this.stationRepository = stationRepository;
    this.vehicleRealtimeService = vehicleRealtimeService;
    this.vehicleWaitTimeEstimator = vehicleWaitTimeEstimator;
  }

  @Transactional(readOnly = true)
  public Page<VehicleResponse> listByStation(long stationId, boolean activeOnly, Pageable pageable) {
    var station =
        stationRepository
            .findById(stationId)
            .orElseThrow(
                () ->
                    new BusinessException(HttpStatus.NOT_FOUND, "STATION_NOT_FOUND", "Gare introuvable"));
    if (station.isArchived()) {
      throw new BusinessException(HttpStatus.NOT_FOUND, "STATION_NOT_FOUND", "Gare introuvable");
    }
    Page<Vehicle> page =
        activeOnly
            ? vehicleRepository.findByStation_IdAndStatusNotAndArchivedFalse(
                stationId, VehicleStatus.PARTI, pageable)
            : vehicleRepository.findByStation_IdAndArchivedFalse(stationId, pageable);
    Instant now = Instant.now();
    List<Vehicle> vehicles = page.getContent();
    Map<Long, Integer> capacities = new HashMap<>();
    for (Vehicle vehicle : vehicles) {
      capacities.put(vehicle.getId(), vehicle.getCapacity());
    }
    Map<Long, SeatCounters> counters = loadSeatCounters(capacities);
    return page.map(v -> toResponse(v, now, counters.get(v.getId())));
  }

  @Transactional
  public VehicleResponse updateStatus(long vehicleId, UpdateVehicleStatusRequest request) {
    Vehicle vehicle =
        vehicleRepository
            .findById(vehicleId)
            .orElseThrow(
                () ->
                    new BusinessException(HttpStatus.NOT_FOUND, "VEHICLE_NOT_FOUND", "Véhicule introuvable"));
    if (vehicle.isArchived()) {
      throw new BusinessException(
          HttpStatus.CONFLICT, "VEHICLE_ARCHIVED", "Ce véhicule n’est plus actif.");
    }
    vehicle.setStatus(request.status());
    Vehicle saved = vehicleRepository.save(vehicle);
    VehicleResponse dto = toResponse(saved, Instant.now(), null);
    vehicleRealtimeService.broadcastUpdate(saved.getStation().getId(), dto);
    return dto;
  }

  @Transactional
  public VehicleResponse updateLocation(long vehicleId, UpdateVehicleLocationRequest request) {
    Vehicle vehicle =
        vehicleRepository
            .findById(vehicleId)
            .orElseThrow(
                () ->
                    new BusinessException(HttpStatus.NOT_FOUND, "VEHICLE_NOT_FOUND", "Véhicule introuvable"));
    if (vehicle.isArchived()) {
      throw new BusinessException(
          HttpStatus.CONFLICT, "VEHICLE_ARCHIVED", "Ce véhicule n’est plus actif.");
    }
    vehicle.updateLocation(request.latitude(), request.longitude(), Instant.now());
    Vehicle saved = vehicleRepository.save(vehicle);
    VehicleResponse dto = toResponse(saved, Instant.now(), null);
    vehicleRealtimeService.broadcastUpdate(saved.getStation().getId(), dto);
    return dto;
  }

  @Transactional(readOnly = true)
  public List<VehicleResponse> listLiveForMap() {
    return vehicleRepository
        .findByStatusNotAndCurrentLatitudeIsNotNullAndCurrentLongitudeIsNotNullAndArchivedFalse(
            VehicleStatus.PARTI)
        .stream()
        .map(v -> toResponse(v, Instant.now(), null))
        .toList();
  }

  private VehicleResponse toResponse(Vehicle vehicle, Instant now, SeatCounters counters) {
    VehicleResponse response =
        VehicleResponse.from(vehicle)
            .withEstimatedWaitMinutes(vehicleWaitTimeEstimator.estimateMinutes(vehicle, now));
    if (counters == null) {
      return response;
    }
    return response.withSeatCounters(counters.occupiedSeats(), counters.unavailableSeats(), counters.availableSeats());
  }

  private Map<Long, SeatCounters> loadSeatCounters(Map<Long, Integer> capacitiesByVehicleId) {
    if (capacitiesByVehicleId.isEmpty()) {
      return Map.of();
    }
    Collection<Long> vehicleIds = capacitiesByVehicleId.keySet();
    Map<Long, SeatCounters> map = new HashMap<>();
    bookingRepository
        .aggregateSeatCountersByVehicleIds(
            vehicleIds, BookingStatus.CONFIRMED, List.of(BookingStatus.CONFIRMED, BookingStatus.PENDING_PAYMENT))
        .forEach(
            item -> {
              int occupied = (int) item.getConfirmedSeats();
              int unavailable = (int) item.getBlockedSeats();
              int cap = capacitiesByVehicleId.getOrDefault(item.getVehicleId(), 0);
              int available = Math.max(0, cap - unavailable);
              map.put(item.getVehicleId(), new SeatCounters(occupied, unavailable, available));
            });
    for (var entry : capacitiesByVehicleId.entrySet()) {
      map.putIfAbsent(entry.getKey(), new SeatCounters(0, 0, entry.getValue()));
    }
    return map;
  }

  private record SeatCounters(int occupiedSeats, int unavailableSeats, int availableSeats) {}
}
