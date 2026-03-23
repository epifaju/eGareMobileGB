package com.garemobilegb.search.service;

import com.garemobilegb.booking.domain.BookingStatus;
import com.garemobilegb.booking.repository.BookingRepository;
import com.garemobilegb.search.dto.DestinationSuggestionResponse;
import com.garemobilegb.search.support.SearchSynonyms;
import com.garemobilegb.shared.exceptions.BusinessException;
import com.garemobilegb.vehicle.domain.Vehicle;
import com.garemobilegb.vehicle.domain.VehicleStatus;
import com.garemobilegb.vehicle.dto.VehicleResponse;
import com.garemobilegb.vehicle.repository.VehicleRepository;
import com.garemobilegb.vehicle.service.VehicleWaitTimeEstimator;
import com.garemobilegb.vehicle.spec.VehicleSpecifications;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SearchService {

  private static final int SUGGEST_MIN_LEN = 2;
  private static final int SUGGEST_MAX_LEN = 120;
  private static final int SUGGEST_PAGE_SIZE = 20;

  private final VehicleRepository vehicleRepository;
  private final BookingRepository bookingRepository;
  private final VehicleWaitTimeEstimator vehicleWaitTimeEstimator;

  public SearchService(
      VehicleRepository vehicleRepository,
      BookingRepository bookingRepository,
      VehicleWaitTimeEstimator vehicleWaitTimeEstimator) {
    this.vehicleRepository = vehicleRepository;
    this.bookingRepository = bookingRepository;
    this.vehicleWaitTimeEstimator = vehicleWaitTimeEstimator;
  }

  @Transactional(readOnly = true)
  public List<DestinationSuggestionResponse> suggestDestinations(String rawQuery) {
    String q = normalizeSuggestQuery(rawQuery);
    if (q.length() < SUGGEST_MIN_LEN) {
      throw new BusinessException(
          HttpStatus.BAD_REQUEST,
          "QUERY_TOO_SHORT",
          "Saisissez au moins " + SUGGEST_MIN_LEN + " caractères.");
    }
    String qFold = SearchSynonyms.stripAccents(q);
    final String filterFolded = qFold.length() >= SUGGEST_MIN_LEN ? qFold : q;
    LinkedHashSet<String> candidates = new LinkedHashSet<>();
    for (String term : SearchSynonyms.expandNormalizedQuery(q)) {
      if (term.length() < SUGGEST_MIN_LEN) {
        continue;
      }
      vehicleRepository
          .findDistinctRouteLabelsContaining(term, PageRequest.of(0, SUGGEST_PAGE_SIZE))
          .getContent()
          .forEach(candidates::add);
    }
    return candidates.stream()
        .filter(label -> SearchSynonyms.routeLabelMatchesFolded(label, filterFolded))
        .sorted()
        .limit(SUGGEST_PAGE_SIZE)
        .map(DestinationSuggestionResponse::new)
        .toList();
  }

  @Transactional(readOnly = true)
  public Page<VehicleResponse> searchVehicles(
      Long stationId,
      String destinationQuery,
      VehicleStatus status,
      Integer minFareXof,
      Integer maxFareXof,
      Instant departureAfter,
      Instant departureBefore,
      boolean activeOnly,
      Pageable pageable) {
    if (minFareXof != null && maxFareXof != null && minFareXof > maxFareXof) {
      throw new BusinessException(
          HttpStatus.BAD_REQUEST,
          "FARE_RANGE_INVALID",
          "Le tarif minimum ne peut pas dépasser le tarif maximum.");
    }
    var spec =
        VehicleSpecifications.forPassengerSearch(
            stationId,
            destinationQuery,
            status,
            minFareXof,
            maxFareXof,
            departureAfter,
            departureBefore,
            activeOnly);
    Instant now = Instant.now();
    Page<Vehicle> page = vehicleRepository.findAll(spec, pageable);
    Map<Long, Integer> capacities = new HashMap<>();
    for (Vehicle vehicle : page.getContent()) {
      capacities.put(vehicle.getId(), vehicle.getCapacity());
    }
    Map<Long, SeatCounters> counters = loadSeatCounters(capacities);
    return page.map(
        v -> {
          VehicleResponse response =
              VehicleResponse.from(v)
                  .withEstimatedWaitMinutes(vehicleWaitTimeEstimator.estimateMinutes(v, now));
          SeatCounters seatCounters = counters.get(v.getId());
          if (seatCounters == null) {
            return response;
          }
          return response.withSeatCounters(
              seatCounters.occupiedSeats(),
              seatCounters.unavailableSeats(),
              seatCounters.availableSeats());
        });
  }

  private static String normalizeSuggestQuery(String raw) {
    if (raw == null) {
      return "";
    }
    String t = raw.trim();
    if (t.length() > SUGGEST_MAX_LEN) {
      t = t.substring(0, SUGGEST_MAX_LEN);
    }
    return t.replace("%", "").replace("_", "").toLowerCase(Locale.ROOT);
  }

  private Map<Long, SeatCounters> loadSeatCounters(Map<Long, Integer> capacitiesByVehicleId) {
    if (capacitiesByVehicleId.isEmpty()) {
      return Map.of();
    }
    Map<Long, SeatCounters> map = new HashMap<>();
    bookingRepository
        .aggregateSeatCountersByVehicleIds(
            capacitiesByVehicleId.keySet(),
            BookingStatus.CONFIRMED,
            List.of(BookingStatus.CONFIRMED, BookingStatus.PENDING_PAYMENT))
        .forEach(
            item -> {
              int occupied = (int) item.getConfirmedSeats();
              int unavailable = (int) item.getBlockedSeats();
              int cap = capacitiesByVehicleId.getOrDefault(item.getVehicleId(), 0);
              int available = Math.max(0, cap - unavailable);
              map.put(item.getVehicleId(), new SeatCounters(occupied, unavailable, available));
            });
    for (var entry : capacitiesByVehicleId.entrySet()) {
      map.putIfAbsent(entry.getKey(), new SeatCounters(0, 0, entry.getValue()));
    }
    return map;
  }

  private record SeatCounters(int occupiedSeats, int unavailableSeats, int availableSeats) {}
}
