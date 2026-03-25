package com.garemobilegb.booking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.garemobilegb.booking.domain.PaymentProvider;
import jakarta.validation.constraints.NotNull;

/** Corps {@code POST /api/bookings/{id}/payment/initiate}. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentInitiateRequest(
    @NotNull PaymentProvider provider,
    /** Optionnel ; peut aussi être passé en en-tête {@code Idempotency-Key}. */
    String idempotencyKey,
    /**
     * Si vrai (prod uniquement), tente les passerelles dans l’ordre Orange → Wave → MTN jusqu’à
     * obtenir une URL ou épuiser la liste.
     */
    Boolean tryFallback) {

  public PaymentInitiateRequest(PaymentProvider provider) {
    this(provider, null, null);
  }
}
