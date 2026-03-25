package com.garemobilegb.booking.dto;

import com.garemobilegb.booking.domain.Booking;
import com.garemobilegb.booking.domain.BookingStatus;
import com.garemobilegb.booking.domain.PaymentStatus;
import com.garemobilegb.shared.util.PhoneMask;
import java.time.Instant;

/** Ligne de manifeste (conducteur / admin). */
public record ManifestPassengerResponse(
    long bookingId,
    Integer seatNumber,
    String phoneMasked,
    BookingStatus bookingStatus,
    PaymentStatus paymentStatus,
    Instant boardingValidatedAt) {

  public static ManifestPassengerResponse from(Booking b) {
    var p = b.getPayment();
    return new ManifestPassengerResponse(
        b.getId(),
        b.getSeatNumber(),
        PhoneMask.mask(b.getUser().getPhoneNumber()),
        b.getStatus(),
        p != null ? p.getStatus() : PaymentStatus.PENDING,
        b.getBoardingValidatedAt());
  }
}
