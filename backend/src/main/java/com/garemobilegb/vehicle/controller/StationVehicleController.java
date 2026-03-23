package com.garemobilegb.vehicle.controller;

import com.garemobilegb.vehicle.dto.VehicleResponse;
import com.garemobilegb.vehicle.service.VehicleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stations")
public class StationVehicleController {

  private final VehicleService vehicleService;

  public StationVehicleController(VehicleService vehicleService) {
    this.vehicleService = vehicleService;
  }

  /**
   * Véhicules rattachés à une gare. Par défaut on exclut les départs (statut PARTI).
   *
   * @param activeOnly si true (défaut), exclut les véhicules déjà partis.
   */
  @GetMapping("/{stationId}/vehicles")
  public Page<VehicleResponse> listForStation(
      @PathVariable long stationId,
      @RequestParam(defaultValue = "true") boolean activeOnly,
      @PageableDefault(size = 50, sort = "departureScheduledAt") Pageable pageable) {
    return vehicleService.listByStation(stationId, activeOnly, pageable);
  }
}
