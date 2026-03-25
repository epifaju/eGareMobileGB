package com.garemobilegb.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Si {@code true}, la réservation est confirmée tout de suite (sans passerelle), utile pour dev
 * rapide. Par défaut {@code false} : {@code PENDING_PAYMENT} puis initiate + webhook (Phase 3).
 */
@ConfigurationProperties(prefix = "app.booking")
public record BookingProperties(boolean autoConfirmWithoutPaymentGateway) {}
