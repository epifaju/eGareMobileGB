package com.garemobilegb.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Passerelles Mobile Money : sandbox vs prod, secret webhooks, URL publique pour pages de
 * paiement (deep links / simulateur).
 */
@ConfigurationProperties(prefix = "app.payment")
public record PaymentGatewayProperties(
    /** {@code sandbox} ou {@code production} */
    String mode,
    /** Secret partagé : en-tête sandbox, HMAC corps en prod. */
    String webhookSecret,
    /**
     * Base URL exposée aux clients (ex. {@code https://api.example.com} ou LAN pour tests mobile).
     */
    String publicBaseUrl) {

  public boolean sandbox() {
    return mode != null && mode.equalsIgnoreCase("sandbox");
  }
}
