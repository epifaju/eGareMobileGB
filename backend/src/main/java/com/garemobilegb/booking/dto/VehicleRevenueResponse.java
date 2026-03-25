package com.garemobilegb.booking.dto;

import java.math.BigDecimal;
import java.time.Instant;

/** Agrégation revenus (paiements PAID) pour un véhicule sur une période. */
public record VehicleRevenueResponse(
    long vehicleId,
    String registrationCode,
    Instant fromInclusive,
    Instant toInclusive,
    BigDecimal totalAmount,
    String currency,
    long paidBookingCount) {}
