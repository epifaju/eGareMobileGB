package com.garemobilegb.admin.service;

import com.garemobilegb.admin.dto.AdminVehicleResponse;
import com.garemobilegb.admin.dto.CreateAdminVehicleRequest;
import com.garemobilegb.admin.dto.UpdateAdminVehicleRequest;
import com.garemobilegb.audit.service.AdminAuditLogService;
import com.garemobilegb.shared.exceptions.BusinessException;
import com.garemobilegb.station.domain.Station;
import com.garemobilegb.station.repository.StationRepository;
import com.garemobilegb.vehicle.domain.Vehicle;
import com.garemobilegb.vehicle.domain.VehicleStatus;
import com.garemobilegb.vehicle.dto.VehicleResponse;
import com.garemobilegb.vehicle.repository.VehicleRepository;
import com.garemobilegb.vehicle.service.VehicleRealtimeService;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminVehicleService {

  private final VehicleRepository vehicleRepository;
  private final StationRepository stationRepository;
  private final VehicleRealtimeService vehicleRealtimeService;
  private final AdminAuditLogService adminAuditLogService;

  public AdminVehicleService(
      VehicleRepository vehicleRepository,
      StationRepository stationRepository,
      VehicleRealtimeService vehicleRealtimeService,
      AdminAuditLogService adminAuditLogService) {
    this.vehicleRepository = vehicleRepository;
    this.stationRepository = stationRepository;
    this.vehicleRealtimeService = vehicleRealtimeService;
    this.adminAuditLogService = adminAuditLogService;
  }

  @Transactional(readOnly = true)
  public Page<AdminVehicleResponse> list(Long stationId, boolean includeArchived, Pageable pageable) {
    Page<Vehicle> page;
    if (stationId != null) {
      if (!stationRepository.existsById(stationId)) {
        throw new BusinessException(HttpStatus.NOT_FOUND, "STATION_NOT_FOUND", "Gare introuvable");
      }
      page =
          includeArchived
              ? vehicleRepository.findByStation_Id(stationId, pageable)
              : vehicleRepository.findByStation_IdAndArchivedFalse(stationId, pageable);
    } else {
      page =
          includeArchived
              ? vehicleRepository.findAll(pageable)
              : vehicleRepository.findByArchivedFalse(pageable);
    }
    return page.map(AdminVehicleResponse::from);
  }

  @Transactional
  public AdminVehicleResponse create(long stationId, CreateAdminVehicleRequest req, long actorUserId) {
    Station station = loadStationForWrite(stationId);
    int cap = req.seatLayout().capacity();
    if (req.occupiedSeats() > cap) {
      throw new BusinessException(
          HttpStatus.BAD_REQUEST,
          "OCCUPIED_EXCEEDS_CAPACITY",
          "Les places occupées ne peuvent pas dépasser la capacité du gabarit.");
    }
    VehicleStatus st = req.status() != null ? req.status() : VehicleStatus.EN_FILE;
    if (st == VehicleStatus.PARTI) {
      throw new BusinessException(
          HttpStatus.BAD_REQUEST,
          "INVALID_INITIAL_STATUS",
          "Créez le véhicule avec un statut opérationnel ; utilisez l’archivage pour retirer du service.");
    }
    Vehicle v =
        new Vehicle(
            station,
            req.registrationCode().trim(),
            req.routeLabel().trim(),
            cap,
            req.occupiedSeats(),
            req.seatLayout(),
            st,
            req.departureScheduledAt(),
            station.getLatitude(),
            station.getLongitude(),
            Instant.now(),
            req.fareAmountXof());
    Vehicle saved = vehicleRepository.save(v);
    adminAuditLogService.record(
        actorUserId,
        "VEHICLE_CREATED",
        "Vehicle",
        saved.getId(),
        Map.of(
            "stationId",
            stationId,
            "registrationCode",
            saved.getRegistrationCode(),
            "routeLabel",
            saved.getRouteLabel()));
    broadcast(saved);
    return AdminVehicleResponse.from(saved);
  }

  @Transactional
  public AdminVehicleResponse update(long vehicleId, UpdateAdminVehicleRequest req, long actorUserId) {
    Vehicle v =
        vehicleRepository
            .findById(vehicleId)
            .orElseThrow(
                () ->
                    new BusinessException(
                        HttpStatus.NOT_FOUND, "VEHICLE_NOT_FOUND", "Véhicule introuvable"));
    Long oldStationId = v.getStation().getId();
    Map<String, Object> changes = new HashMap<>();

    if (req.registrationCode() != null && !req.registrationCode().isBlank()) {
      changes.put("registrationCode", req.registrationCode().trim());
      v.setRegistrationCode(req.registrationCode().trim());
    }
    if (req.routeLabel() != null && !req.routeLabel().isBlank()) {
      changes.put("routeLabel", req.routeLabel().trim());
      v.setRouteLabel(req.routeLabel().trim());
    }
    if (req.fareAmountXof() != null) {
      changes.put("fareAmountXof", req.fareAmountXof());
      v.setFareAmountXof(req.fareAmountXof());
    }
    if (req.departureScheduledAt() != null) {
      changes.put("departureScheduledAt", req.departureScheduledAt().toString());
      v.setDepartureScheduledAt(req.departureScheduledAt());
    }
    if (req.stationId() != null) {
      Station newSt = loadStationForWrite(req.stationId());
      changes.put("stationId", req.stationId());
      v.setStation(newSt);
    }
    if (req.seatLayout() != null) {
      changes.put("seatLayout", req.seatLayout().name());
      v.setSeatLayout(req.seatLayout());
      v.setCapacity(req.seatLayout().capacity());
    }
    if (req.occupiedSeats() != null) {
      changes.put("occupiedSeats", req.occupiedSeats());
      v.setOccupiedSeats(req.occupiedSeats());
    }
    if (req.status() != null) {
      if (req.status() == VehicleStatus.PARTI && !v.isArchived()) {
        throw new BusinessException(
            HttpStatus.BAD_REQUEST,
            "USE_ARCHIVE_ENDPOINT",
            "Pour marquer un départ définitif et retirer le véhicule du service, utilisez POST .../archive.");
      }
      changes.put("status", req.status().name());
      v.setStatus(req.status());
    }

    if (v.getOccupiedSeats() > v.getCapacity()) {
      throw new BusinessException(
          HttpStatus.BAD_REQUEST,
          "OCCUPIED_EXCEEDS_CAPACITY",
          "Les places occupées dépassent la capacité.");
    }

    if (changes.isEmpty()) {
      return AdminVehicleResponse.from(v);
    }

    Vehicle saved = vehicleRepository.save(v);
    adminAuditLogService.record(actorUserId, "VEHICLE_UPDATED", "Vehicle", saved.getId(), changes);
    broadcast(saved);
    if (req.stationId() != null && !oldStationId.equals(saved.getStation().getId())) {
      vehicleRealtimeService.broadcastUpdate(oldStationId, VehicleResponse.from(saved));
    }
    return AdminVehicleResponse.from(saved);
  }

  @Transactional
  public AdminVehicleResponse archive(long vehicleId, long actorUserId) {
    Vehicle v =
        vehicleRepository
            .findById(vehicleId)
            .orElseThrow(
                () ->
                    new BusinessException(
                        HttpStatus.NOT_FOUND, "VEHICLE_NOT_FOUND", "Véhicule introuvable"));
    if (v.isArchived()) {
      return AdminVehicleResponse.from(v);
    }
    v.setStatus(VehicleStatus.PARTI);
    v.setArchived(true);
    Vehicle saved = vehicleRepository.save(v);
    adminAuditLogService.record(
        actorUserId,
        "VEHICLE_ARCHIVED",
        "Vehicle",
        saved.getId(),
        Map.of("registrationCode", saved.getRegistrationCode(), "stationId", saved.getStation().getId()));
    broadcast(saved);
    return AdminVehicleResponse.from(saved);
  }

  private Station loadStationForWrite(long stationId) {
    Station station =
        stationRepository
            .findById(stationId)
            .orElseThrow(
                () ->
                    new BusinessException(HttpStatus.NOT_FOUND, "STATION_NOT_FOUND", "Gare introuvable"));
    if (station.isArchived()) {
      throw new BusinessException(
          HttpStatus.CONFLICT, "STATION_ARCHIVED", "Cette gare est archivée : affectation impossible.");
    }
    return station;
  }

  private void broadcast(Vehicle saved) {
    vehicleRealtimeService.broadcastUpdate(saved.getStation().getId(), VehicleResponse.from(saved));
  }
}
