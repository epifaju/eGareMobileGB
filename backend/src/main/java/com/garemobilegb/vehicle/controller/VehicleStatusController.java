package com.garemobilegb.vehicle.controller;

import com.garemobilegb.vehicle.dto.UpdateVehicleStatusRequest;
import com.garemobilegb.vehicle.dto.UpdateVehicleLocationRequest;
import com.garemobilegb.vehicle.dto.VehicleResponse;
import com.garemobilegb.vehicle.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleStatusController {

  private final VehicleService vehicleService;

  public VehicleStatusController(VehicleService vehicleService) {
    this.vehicleService = vehicleService;
  }

  /** Mise à jour statut conducteur / admin — déclenche un push WebSocket sur la gare. */
  @PutMapping("/{id}/status")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasAnyRole('AGENT','DRIVER','ADMIN')")
  public VehicleResponse updateStatus(
      @PathVariable long id, @Valid @RequestBody UpdateVehicleStatusRequest request) {
    return vehicleService.updateStatus(id, request);
  }

  /** Mise à jour GPS conducteur / admin — déclenche aussi un push map live. */
  @PutMapping("/{id}/location")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasAnyRole('AGENT','DRIVER','ADMIN')")
  public VehicleResponse updateLocation(
      @PathVariable long id, @Valid @RequestBody UpdateVehicleLocationRequest request) {
    return vehicleService.updateLocation(id, request);
  }
}
