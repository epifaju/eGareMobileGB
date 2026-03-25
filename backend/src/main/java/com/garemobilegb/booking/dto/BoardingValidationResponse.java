package com.garemobilegb.booking.dto;

import com.garemobilegb.booking.domain.Booking;
import java.time.Instant;

/** Réponse après scan QR embarquement (conducteur). */
public record BoardingValidationResponse(
    long bookingId,
    long vehicleId,
    String registrationCode,
    String routeLabel,
    Integer seatNumber,
    boolean alreadyValidated,
    Instant validatedAt) {

  public static BoardingValidationResponse from(Booking b, boolean alreadyValidated) {
    var v = b.getVehicle();
    return new BoardingValidationResponse(
        b.getId(),
        v.getId(),
        v.getRegistrationCode(),
        v.getRouteLabel(),
        b.getSeatNumber(),
        alreadyValidated,
        b.getBoardingValidatedAt());
  }
}
