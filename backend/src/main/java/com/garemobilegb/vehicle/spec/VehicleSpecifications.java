package com.garemobilegb.vehicle.spec;

import com.garemobilegb.search.support.SearchSynonyms;
import com.garemobilegb.vehicle.domain.Vehicle;
import com.garemobilegb.vehicle.domain.VehicleStatus;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class VehicleSpecifications {

  private VehicleSpecifications() {}

  public static Specification<Vehicle> forPassengerSearch(
      Long stationId,
      String destinationSubstring,
      VehicleStatus status,
      Integer minFareXof,
      Integer maxFareXof,
      Instant departureAfter,
      Instant departureBefore,
      boolean activeOnly) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (stationId != null) {
        predicates.add(cb.equal(root.get("station").get("id"), stationId));
      }

      if (destinationSubstring != null && !destinationSubstring.isBlank()) {
        List<Predicate> destOr = new ArrayList<>();
        for (String term : SearchSynonyms.expandDestinationTerms(destinationSubstring)) {
          String safe = term.toLowerCase().replace("%", "").replace("_", "");
          if (safe.length() < 2) {
            continue;
          }
          String pattern = "%" + safe + "%";
          destOr.add(cb.like(cb.lower(root.get("routeLabel")), pattern));
        }
        if (!destOr.isEmpty()) {
          predicates.add(cb.or(destOr.toArray(Predicate[]::new)));
        }
      }

      if (status != null) {
        predicates.add(cb.equal(root.get("status"), status));
      } else if (activeOnly) {
        predicates.add(cb.notEqual(root.get("status"), VehicleStatus.PARTI));
      }

      if (minFareXof != null) {
        predicates.add(cb.isNotNull(root.get("fareAmountXof")));
        predicates.add(cb.greaterThanOrEqualTo(root.get("fareAmountXof"), minFareXof));
      }
      if (maxFareXof != null) {
        predicates.add(cb.isNotNull(root.get("fareAmountXof")));
        predicates.add(cb.lessThanOrEqualTo(root.get("fareAmountXof"), maxFareXof));
      }

      if (departureAfter != null || departureBefore != null) {
        predicates.add(cb.isNotNull(root.get("departureScheduledAt")));
        if (departureAfter != null) {
          predicates.add(
              cb.greaterThanOrEqualTo(root.get("departureScheduledAt"), departureAfter));
        }
        if (departureBefore != null) {
          predicates.add(
              cb.lessThanOrEqualTo(root.get("departureScheduledAt"), departureBefore));
        }
      }

      return cb.and(predicates.toArray(Predicate[]::new));
    };
  }
}
