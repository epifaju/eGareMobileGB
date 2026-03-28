package com.garemobilegb.booking.service;

import com.garemobilegb.booking.domain.BookingStatus;
import com.garemobilegb.booking.domain.Payment;
import com.garemobilegb.booking.domain.PaymentStatus;
import com.garemobilegb.booking.domain.RefundAuditEventType;
import com.garemobilegb.booking.repository.PaymentRepository;
import com.garemobilegb.shared.exceptions.BusinessException;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Stub opérationnel : finalise un remboursement {@link PaymentStatus#REFUND_PENDING} comme si le
 * fournisseur l’avait confirmé (bouton / script admin jusqu’à branchement API réelle).
 */
@Service
public class AdminRefundCompletionService {

  private final PaymentRepository paymentRepository;
  private final RefundAuditService refundAuditService;

  public AdminRefundCompletionService(
      PaymentRepository paymentRepository, RefundAuditService refundAuditService) {
    this.paymentRepository = paymentRepository;
    this.refundAuditService = refundAuditService;
  }

  @Transactional
  public void completeStubRefund(long paymentId, long adminUserId) {
    Payment payment =
        paymentRepository
            .findWithBookingById(paymentId)
            .orElseThrow(
                () ->
                    new BusinessException(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND", "Paiement introuvable."));
    if (payment.getStatus() != PaymentStatus.REFUND_PENDING) {
      throw new BusinessException(
          HttpStatus.CONFLICT,
          "PAYMENT_NOT_REFUND_PENDING",
          "Seuls les paiements en statut REFUND_PENDING peuvent être finalisés (actuel : "
              + payment.getStatus()
              + ").");
    }
    var booking = payment.getBooking();
    if (booking.getStatus() != BookingStatus.CANCELLED) {
      throw new BusinessException(
          HttpStatus.CONFLICT,
          "BOOKING_NOT_CANCELLED",
          "La réservation doit être annulée avant de clôturer le remboursement.");
    }

    Instant now = Instant.now();
    payment.setStatus(PaymentStatus.REFUNDED);
    payment.setRefundedAt(now);
    payment.setRefundProviderRef("admin-stub-" + UUID.randomUUID());
    paymentRepository.save(payment);

    refundAuditService.append(
        booking.getId(),
        adminUserId,
        RefundAuditEventType.ADMIN_REFUND_STUB_COMPLETED,
        "paymentId="
            + paymentId
            + ", refundAmount="
            + payment.getRefundAmount()
            + ", ref="
            + payment.getRefundProviderRef());
  }
}
