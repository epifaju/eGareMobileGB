package com.garemobilegb.station.service;

import com.garemobilegb.shared.exceptions.BusinessException;
import com.garemobilegb.station.domain.Station;
import com.garemobilegb.station.dto.LowBandwidthStationResponse;
import com.garemobilegb.station.dto.StationResponse;
import com.garemobilegb.vehicle.domain.VehicleStatus;
import com.garemobilegb.vehicle.repository.VehicleRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.garemobilegb.station.repository.StationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StationService {

  private final StationRepository stationRepository;
  private final VehicleRepository vehicleRepository;

  public StationService(StationRepository stationRepository, VehicleRepository vehicleRepository) {
    this.stationRepository = stationRepository;
    this.vehicleRepository = vehicleRepository;
  }

  @Transactional(readOnly = true)
  public Page<StationResponse> list(Pageable pageable) {
    return stationRepository.findByArchivedFalse(pageable).map(StationResponse::from);
  }

  @Transactional(readOnly = true)
  public StationResponse getById(long id) {
    Station station =
        stationRepository
            .findByIdAndArchivedFalse(id)
            .orElseThrow(
                () ->
                    new BusinessException(HttpStatus.NOT_FOUND, "STATION_NOT_FOUND", "Gare introuvable"));
    return StationResponse.from(station);
  }

  @Transactional(readOnly = true)
  public Page<LowBandwidthStationResponse> listLowBandwidth(Pageable pageable) {
    Page<Station> page = stationRepository.findByArchivedFalse(pageable);
    List<Long> stationIds = page.stream().map(Station::getId).toList();
    Map<Long, VehicleRepository.StationActiveVehicleAggregate> aggByStation = new HashMap<>();
    if (!stationIds.isEmpty()) {
      vehicleRepository
          .aggregateActiveByStationIds(stationIds, VehicleStatus.PARTI)
          .forEach(agg -> aggByStation.put(agg.getStationId(), agg));
    }
    return page.map(
        s -> {
          var agg = aggByStation.get(s.getId());
          long active = agg != null ? agg.getActiveVehicles() : 0L;
          var next = agg != null ? agg.getNextDepartureAt() : null;
          Integer minFare = agg != null ? agg.getMinFareXof() : null;
          return new LowBandwidthStationResponse(s.getId(), s.getName(), s.getCity(), active, next, minFare);
        });
  }
}
