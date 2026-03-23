package com.garemobilegb.station.controller;

import com.garemobilegb.station.dto.StationResponse;
import com.garemobilegb.station.dto.LowBandwidthStationResponse;
import com.garemobilegb.station.service.StationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stations")
public class StationController {

  private final StationService stationService;

  public StationController(StationService stationService) {
    this.stationService = stationService;
  }

  /** Liste paginée des gares (public). */
  @GetMapping
  public Page<StationResponse> list(
      @PageableDefault(size = 20, sort = "name") Pageable pageable) {
    return stationService.list(pageable);
  }

  @GetMapping("/{id}")
  public StationResponse getById(@PathVariable long id) {
    return stationService.getById(id);
  }

  /**
   * Vue alternative "réseau faible" : payload compact (<50KB cible côté client avec page limitée).
   */
  @GetMapping("/low-bandwidth")
  public Page<LowBandwidthStationResponse> listLowBandwidth(
      @PageableDefault(size = 20, sort = "name") Pageable pageable) {
    return stationService.listLowBandwidth(pageable);
  }
}
