package com.garemobilegb.shared.config;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Phase 4 — annulation / remboursement (aligné PRD : fenêtre avant départ, % remboursé, délai
 * opérationnel).
 */
@ConfigurationProperties(prefix = "app.booking.refund")
public record RefundProperties(
    /** Minutes minimales avant l’heure de départ annoncée pour pouvoir annuler. */
    int minMinutesBeforeDeparture,
    /** Part du montant payé remboursée (ex. 0.80 = 80 %). */
    BigDecimal refundPercentOfPaidAmount,
    /**
     * Délai cible côté métier pour finaliser le remboursement opérateur (information / SLA interne).
     */
    int refundProviderSlaHours,
    /**
     * Si le véhicule n’a pas d’heure de départ ({@code departureScheduledAt} nul), autoriser
     * l’annulation sans contrôle temporel.
     */
    boolean allowCancelWhenDepartureUnknown,
    /**
     * Sans API remboursement réelle : marquer le remboursement comme terminé tout de suite (dev /
     * tests).
     */
    boolean simulateProviderRefundInstant) {

  public int minMinutesOrDefault() {
    return minMinutesBeforeDeparture > 0 ? minMinutesBeforeDeparture : 30;
  }

  public int slaHoursOrDefault() {
    return refundProviderSlaHours > 0 ? refundProviderSlaHours : 48;
  }

  /** Ratio effectif (défaut 80 % si non lié). */
  public BigDecimal refundRatio() {
    if (refundPercentOfPaidAmount == null) {
      return new BigDecimal("0.80");
    }
    return refundPercentOfPaidAmount;
  }
}
