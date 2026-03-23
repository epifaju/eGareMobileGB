package com.garemobilegb.search.controller;

import com.garemobilegb.search.service.SearchService;
import com.garemobilegb.vehicle.domain.VehicleStatus;
import com.garemobilegb.vehicle.dto.VehicleResponse;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
public class SearchController {

  private final SearchService searchService;

  public SearchController(SearchService searchService) {
    this.searchService = searchService;
  }

  /**
   * Recherche de véhicules par destination (sous-chaîne sur {@code routeLabel}), filtres optionnels
   * statut / tarif XOF / fenêtre de départ.
   */
  @GetMapping("/vehicles")
  public Page<VehicleResponse> searchVehicles(
      @RequestParam(required = false) Long stationId,
      @RequestParam(required = false) String q,
      @RequestParam(required = false) VehicleStatus status,
      @RequestParam(required = false) Integer minFareXof,
      @RequestParam(required = false) Integer maxFareXof,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          Instant departureAfter,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          Instant departureBefore,
      @RequestParam(defaultValue = "true") boolean activeOnly,
      @PageableDefault(size = 50, sort = "departureScheduledAt", direction = Sort.Direction.ASC)
          Pageable pageable) {
    return searchService.searchVehicles(
        stationId,
        q,
        status,
        minFareXof,
        maxFareXof,
        departureAfter,
        departureBefore,
        activeOnly,
        pageable);
  }
}
