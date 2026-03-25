package com.garemobilegb.booking.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Corps typique webhook sandbox / prod (adapter les champs réels Orange/Wave/MTN en prod).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentWebhookRequest(
    long bookingId,
    String externalTransactionId,
    String status,
    /** Optionnel : même valeur que {@link com.garemobilegb.booking.domain.PaymentProvider#name()}. */
    String provider) {}
