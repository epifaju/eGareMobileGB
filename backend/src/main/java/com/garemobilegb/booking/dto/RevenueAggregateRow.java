package com.garemobilegb.booking.dto;

import java.math.BigDecimal;

/** Résultat de l’agrégation revenus (une ligne). */
public record RevenueAggregateRow(BigDecimal totalAmount, Long bookingCount) {}
