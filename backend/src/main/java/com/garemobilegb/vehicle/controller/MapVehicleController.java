package com.garemobilegb.vehicle.controller;

import com.garemobilegb.vehicle.dto.VehicleResponse;
import com.garemobilegb.vehicle.service.VehicleService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/map")
public class MapVehicleController {

  private final VehicleService vehicleService;

  public MapVehicleController(VehicleService vehicleService) {
    this.vehicleService = vehicleService;
  }

  /** Flux « live map » : véhicules actifs avec coordonnées connues. */
  @GetMapping("/vehicles")
  public List<VehicleResponse> listLiveVehicles() {
    return vehicleService.listLiveForMap();
  }
}
