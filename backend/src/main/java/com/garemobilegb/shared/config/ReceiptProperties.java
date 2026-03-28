package com.garemobilegb.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Phase 5 — reçu PDF : texte SMS (WhatsApp Business API non branché : même message peut être réutilisé
 * manuellement).
 */
@ConfigurationProperties(prefix = "app.booking.receipt")
public record ReceiptProperties(
    /**
     * Remplacements : {@code {bookingId}}, {@code {route}}, {@code {station}}, {@code {amount}},
     * {@code {currency}}.
     */
    String smsTemplate) {

  public String smsTemplateOrDefault() {
    if (smsTemplate != null && !smsTemplate.isBlank()) {
      return smsTemplate;
    }
    return "eGare : votre reçu pour la réservation n°{bookingId} ({route}, {station}) est disponible. "
        + "Ouvrez l’application > Mes réservations pour télécharger le PDF.";
  }
}
