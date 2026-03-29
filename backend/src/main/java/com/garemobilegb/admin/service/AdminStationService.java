package com.garemobilegb.admin.service;

import com.garemobilegb.admin.dto.AdminStationResponse;
import com.garemobilegb.admin.dto.AdminStationWriteRequest;
import com.garemobilegb.audit.service.AdminAuditLogService;
import com.garemobilegb.shared.exceptions.BusinessException;
import com.garemobilegb.station.domain.Station;
import com.garemobilegb.station.repository.StationRepository;
import com.garemobilegb.vehicle.domain.Vehicle;
import com.garemobilegb.vehicle.domain.VehicleStatus;
import com.garemobilegb.vehicle.dto.VehicleResponse;
import com.garemobilegb.vehicle.repository.VehicleRepository;
import com.garemobilegb.vehicle.service.VehicleRealtimeService;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminStationService {

  private final StationRepository stationRepository;
  private final VehicleRepository vehicleRepository;
  private final VehicleRealtimeService vehicleRealtimeService;
  private final AdminAuditLogService adminAuditLogService;

  public AdminStationService(
      StationRepository stationRepository,
      VehicleRepository vehicleRepository,
      VehicleRealtimeService vehicleRealtimeService,
      AdminAuditLogService adminAuditLogService) {
    this.stationRepository = stationRepository;
    this.vehicleRepository = vehicleRepository;
    this.vehicleRealtimeService = vehicleRealtimeService;
    this.adminAuditLogService = adminAuditLogService;
  }

  @Transactional(readOnly = true)
  public Page<AdminStationResponse> list(boolean includeArchived, Pageable pageable) {
    Page<Station> page =
        includeArchived ? stationRepository.findAll(pageable) : stationRepository.findByArchivedFalse(pageable);
    return page.map(AdminStationResponse::from);
  }

  @Transactional
  public AdminStationResponse create(AdminStationWriteRequest req, long actorUserId) {
    String desc = req.description() != null ? req.description() : "";
    Station s =
        new Station(
            req.name().trim(),
            req.city() != null ? req.city().trim() : "",
            req.latitude(),
            req.longitude(),
            desc);
    Station saved = stationRepository.save(s);
    adminAuditLogService.record(
        actorUserId,
        "STATION_CREATED",
        "Station",
        saved.getId(),
        Map.of("name", saved.getName(), "city", saved.getCity() != null ? saved.getCity() : ""));
    return AdminStationResponse.from(saved);
  }

  @Transactional
  public AdminStationResponse update(long stationId, AdminStationWriteRequest req, long actorUserId) {
    Station s =
        stationRepository
            .findById(stationId)
            .orElseThrow(
                () ->
                    new BusinessException(HttpStatus.NOT_FOUND, "STATION_NOT_FOUND", "Gare introuvable"));
    s.setName(req.name().trim());
    s.setCity(req.city() != null ? req.city().trim() : "");
    s.setLatitude(req.latitude());
    s.setLongitude(req.longitude());
    s.setDescription(req.description() != null ? req.description() : "");
    Station saved = stationRepository.save(s);
    adminAuditLogService.record(
        actorUserId, "STATION_UPDATED", "Station", saved.getId(), Map.of("name", saved.getName()));
    return AdminStationResponse.from(saved);
  }

  @Transactional
  public AdminStationResponse archive(long stationId, long actorUserId) {
    Station s =
        stationRepository
            .findById(stationId)
            .orElseThrow(
                () ->
                    new BusinessException(HttpStatus.NOT_FOUND, "STATION_NOT_FOUND", "Gare introuvable"));
    if (s.isArchived()) {
      return AdminStationResponse.from(s);
    }
    List<Vehicle> atStation = vehicleRepository.findAllByStation_Id(stationId);
    int cascaded = 0;
    for (Vehicle v : atStation) {
      if (!v.isArchived()) {
        v.setStatus(VehicleStatus.PARTI);
        v.setArchived(true);
        cascaded++;
        vehicleRepository.save(v);
        vehicleRealtimeService.broadcastUpdate(stationId, VehicleResponse.from(v));
      }
    }
    s.setArchived(true);
    Station saved = stationRepository.save(s);
    adminAuditLogService.record(
        actorUserId,
        "STATION_ARCHIVED",
        "Station",
        saved.getId(),
        Map.of("name", saved.getName(), "vehiclesArchived", cascaded));
    return AdminStationResponse.from(saved);
  }
}
