package com.garemobilegb.booking.service;

import com.garemobilegb.shared.config.RefundProperties;
import com.garemobilegb.shared.exceptions.BusinessException;
import com.garemobilegb.vehicle.domain.Vehicle;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Fenêtre d’annulation PRD : au moins {@code minMinutesBeforeDeparture} minutes avant le départ
 * prévu, si celui-ci est connu.
 */
@Component
public class BookingCancellationPolicy {

  private final RefundProperties refundProperties;

  public BookingCancellationPolicy(RefundProperties refundProperties) {
    this.refundProperties = refundProperties;
  }

  public void assertMayCancel(Vehicle vehicle, Instant now) {
    if (vehicle.getDepartureScheduledAt() == null) {
      if (refundProperties.allowCancelWhenDepartureUnknown()) {
        return;
      }
      throw new BusinessException(
          HttpStatus.CONFLICT,
          "DEPARTURE_TIME_UNKNOWN",
          "Heure de départ inconnue : annulation impossible pour le moment.");
    }
    Instant deadline =
        vehicle.getDepartureScheduledAt().minus(refundProperties.minMinutesOrDefault(), ChronoUnit.MINUTES);
    if (now.isAfter(deadline)) {
      throw new BusinessException(
          HttpStatus.CONFLICT,
          "CANCELLATION_TOO_LATE",
          "Annulation impossible : moins de "
              + refundProperties.minMinutesOrDefault()
              + " minute(s) avant le départ prévu.");
    }
  }
}
