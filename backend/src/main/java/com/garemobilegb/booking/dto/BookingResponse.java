package com.garemobilegb.booking.dto;

import com.garemobilegb.booking.domain.Booking;
import com.garemobilegb.booking.domain.BookingStatus;
import com.garemobilegb.booking.domain.PaymentProvider;
import com.garemobilegb.booking.domain.PaymentStatus;
import com.garemobilegb.vehicle.domain.VehicleStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record BookingResponse(
    long id,
    long vehicleId,
    long stationId,
    String stationName,
    String registrationCode,
    String routeLabel,
    VehicleStatus vehicleStatus,
    Instant createdAt,
    BookingStatus bookingStatus,
    PaymentStatus paymentStatus,
    BigDecimal amount,
    String currency,
    PaymentProvider paymentProvider,
    Integer seatNumber,
    String qrToken,
    Instant expiresAt) {

  public static BookingResponse from(Booking b) {
    var v = b.getVehicle();
    var station = v.getStation();
    var p = b.getPayment();
    return new BookingResponse(
        b.getId(),
        v.getId(),
        station.getId(),
        station.getName(),
        v.getRegistrationCode(),
        v.getRouteLabel(),
        v.getStatus(),
        b.getCreatedAt(),
        b.getStatus(),
        p != null ? p.getStatus() : PaymentStatus.PENDING,
        p != null ? p.getAmount() : BigDecimal.ZERO,
        p != null ? p.getCurrency() : "XOF",
        p != null ? p.getProvider() : PaymentProvider.INTERNAL,
        b.getSeatNumber(),
        b.getQrToken(),
        b.getExpiresAt());
  }
}
