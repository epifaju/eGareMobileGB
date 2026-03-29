package com.garemobilegb.vehicle.controller;

import com.garemobilegb.booking.dto.ManifestPassengerResponse;
import com.garemobilegb.vehicle.service.VehicleOpsService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleManifestController {

  private final VehicleOpsService vehicleOpsService;

  public VehicleManifestController(VehicleOpsService vehicleOpsService) {
    this.vehicleOpsService = vehicleOpsService;
  }

  /** Manifeste passagers (siège, téléphone masqué, statut embarquement). */
  @GetMapping("/{vehicleId}/manifest")
  @PreAuthorize("hasAnyRole('AGENT','DRIVER','ADMIN')")
  public List<ManifestPassengerResponse> manifest(@PathVariable long vehicleId) {
    return vehicleOpsService.manifestForVehicle(vehicleId);
  }
}
