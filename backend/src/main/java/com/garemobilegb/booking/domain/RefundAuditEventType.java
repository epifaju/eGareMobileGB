package com.garemobilegb.booking.domain;

public enum RefundAuditEventType {
  CANCELLATION_WINDOW_CHECKED,
  REFUND_AMOUNT_CALCULATED,
  REFUND_PROVIDER_INITIATED,
  REFUND_COMPLETED,
  REFUND_PENDING_SLA,
  /** Finalisation manuelle (stub admin) d’un remboursement en attente fournisseur. */
  ADMIN_REFUND_STUB_COMPLETED
}
