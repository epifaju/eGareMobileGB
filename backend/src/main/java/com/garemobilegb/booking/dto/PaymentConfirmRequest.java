package com.garemobilegb.booking.dto;

/**
 * Corps optionnel pour {@code POST /api/payments/{bookingId}/confirm} — réservé aux futurs
 * retours passerelle (référence transaction, etc.).
 */
public record PaymentConfirmRequest(String providerTransactionRef) {}
