package com.garemobilegb.booking.domain;

public enum PaymentStatus {
  PENDING,
  PAID,
  /** Remboursement initié auprès du fournisseur, en attente de confirmation (Phase 4). */
  REFUND_PENDING,
  FAILED,
  REFUNDED
}
