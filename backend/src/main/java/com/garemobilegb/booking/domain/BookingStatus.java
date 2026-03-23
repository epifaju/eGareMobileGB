package com.garemobilegb.booking.domain;

/** Statuts métier réservation (PRD §5.5). */
public enum BookingStatus {
  /** Créée ; paiement en attente (place véhicule non encore comptée si flux paiement séparé). */
  PENDING_PAYMENT,
  /** Paiement validé / réservation effective (place comptée). */
  CONFIRMED,
  /** Annulée par l’utilisateur ou le système. */
  CANCELLED,
  /** TTL dépassée (billet non utilisé). */
  EXPIRED
}
