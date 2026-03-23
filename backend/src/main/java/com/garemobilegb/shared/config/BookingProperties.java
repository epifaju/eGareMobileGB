package com.garemobilegb.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Phase 0 : si {@code true}, la réservation est confirmée et payée en interne (sans passerelle),
 * comme l’ancien flux « réservation immédiate ».
 */
@ConfigurationProperties(prefix = "app.booking")
public record BookingProperties(boolean autoConfirmWithoutPaymentGateway) {}
