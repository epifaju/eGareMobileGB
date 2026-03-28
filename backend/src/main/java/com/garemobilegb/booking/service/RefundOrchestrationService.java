package com.garemobilegb.booking.service;

import com.garemobilegb.booking.domain.Payment;
import com.garemobilegb.booking.domain.PaymentProvider;
import com.garemobilegb.booking.domain.PaymentStatus;
import com.garemobilegb.booking.domain.RefundAuditEventType;
import com.garemobilegb.booking.repository.PaymentRepository;
import com.garemobilegb.shared.config.RefundProperties;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Phase 4 — après annulation d’une réservation payée : calcule le montant remboursable et enclenche
 * le flux fournisseur (stub ou API réelle ultérieure).
 */
@Service
public class RefundOrchestrationService {

  private static final Logger log = LoggerFactory.getLogger(RefundOrchestrationService.class);

  private final RefundProperties refundProperties;
  private final RefundAuditService refundAuditService;
  private final PaymentRepository paymentRepository;

  public RefundOrchestrationService(
      RefundProperties refundProperties,
      RefundAuditService refundAuditService,
      PaymentRepository paymentRepository) {
    this.refundProperties = refundProperties;
    this.refundAuditService = refundAuditService;
    this.paymentRepository = paymentRepository;
  }

  /**
   * @param bookingId pour l’audit
   * @param userId voyageur
   */
  @Transactional
  public void applyRefundAfterCancellation(
      long bookingId, long userId, Payment payment, BigDecimal paidAmount) {
    BigDecimal ratio = refundProperties.refundRatio();
    BigDecimal refundAmount =
        paidAmount.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
    payment.setRefundAmount(refundAmount);
    refundAuditService.append(
        bookingId,
        userId,
        RefundAuditEventType.REFUND_AMOUNT_CALCULATED,
        "paid="
            + paidAmount
            + " "
            + payment.getCurrency()
            + ", ratio="
            + ratio
            + ", refund="
            + refundAmount);

    PaymentProvider provider = payment.getProvider();
    boolean internal = provider == PaymentProvider.INTERNAL;

    if (internal || refundProperties.simulateProviderRefundInstant()) {
      completeRefundImmediately(payment, bookingId, userId, internal ? "INTERNAL" : "SIMULATED");
      return;
    }

    payment.setStatus(PaymentStatus.REFUND_PENDING);
    paymentRepository.save(payment);
    refundAuditService.append(
        bookingId,
        userId,
        RefundAuditEventType.REFUND_PROVIDER_INITIATED,
        "provider="
            + provider
            + ", refundAmount="
            + refundAmount
            + ", slaHours="
            + refundProperties.slaHoursOrDefault()
            + " — en attente d’API remboursement opérateur.");
    refundAuditService.append(
        bookingId,
        userId,
        RefundAuditEventType.REFUND_PENDING_SLA,
        "Remboursement à finaliser sous "
            + refundProperties.slaHoursOrDefault()
            + " h (PRD) côté "
            + provider
            + ".");
    log.info(
        "Remboursement en attente fournisseur bookingId={} montant={} provider={}",
        bookingId,
        refundAmount,
        provider);
  }

  private void completeRefundImmediately(Payment payment, long bookingId, long userId, String refPrefix) {
    Instant now = Instant.now();
    payment.setStatus(PaymentStatus.REFUNDED);
    payment.setRefundedAt(now);
    payment.setRefundProviderRef(refPrefix + "-" + UUID.randomUUID());
    paymentRepository.save(payment);
    refundAuditService.append(
        bookingId,
        userId,
        RefundAuditEventType.REFUND_COMPLETED,
        "refundAmount="
            + payment.getRefundAmount()
            + ", ref="
            + payment.getRefundProviderRef());
  }
}
