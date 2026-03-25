package com.garemobilegb.booking.dto;

import com.garemobilegb.booking.domain.PaymentProvider;
import java.math.BigDecimal;

public record PaymentInitiateResponse(
    /** URL à ouvrir (navigateur / WebView) — sandbox : page HTML locale. */
    String checkoutUrl,
    /** Jeton court (JWT) — identique à celui inclus dans {@code checkoutUrl} en query. */
    String paymentToken,
    BigDecimal amount,
    String currency,
    PaymentProvider provider) {}
