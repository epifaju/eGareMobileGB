package com.garemobilegb.vehicle.controller;

import com.garemobilegb.booking.dto.VehicleRevenueResponse;
import com.garemobilegb.vehicle.service.VehicleOpsService;
import java.time.Instant;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleRevenueController {

  private final VehicleOpsService vehicleOpsService;

  public VehicleRevenueController(VehicleOpsService vehicleOpsService) {
    this.vehicleOpsService = vehicleOpsService;
  }

  /** Revenus (somme paiements PAID) pour un véhicule sur une fenêtre temporelle. */
  @GetMapping("/{vehicleId}/revenue")
  @PreAuthorize("hasAnyRole('AGENT','DRIVER','ADMIN')")
  public VehicleRevenueResponse revenue(
      @PathVariable long vehicleId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
    return vehicleOpsService.revenueForVehicle(vehicleId, from, to);
  }
}
