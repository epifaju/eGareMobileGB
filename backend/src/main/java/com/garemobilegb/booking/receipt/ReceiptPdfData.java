package com.garemobilegb.booking.receipt;

import java.math.BigDecimal;

/** Données figées pour un reçu PDF (testable sans entités JPA). */
public record ReceiptPdfData(
    long bookingId,
    String stationName,
    String routeLabel,
    String registrationCode,
    Integer seatNumber,
    BigDecimal amount,
    String currency,
    String paymentProvider,
    String paymentStatus,
    String bookingStatus,
    String providerRef,
    String issuedAtText,
    String reservationCreatedAtText) {}
