package com.garemobilegb.booking.controller;

import com.garemobilegb.booking.service.AdminRefundCompletionService;
import com.garemobilegb.shared.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Administration — finalisation manuelle des remboursements en attente (stub jusqu’à API
 * opérateur).
 */
@RestController
@RequestMapping("/api/admin/payments")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRefundController {

  private final AdminRefundCompletionService adminRefundCompletionService;

  public AdminRefundController(AdminRefundCompletionService adminRefundCompletionService) {
    this.adminRefundCompletionService = adminRefundCompletionService;
  }

  /**
   * Simule la confirmation fournisseur : passe un paiement de {@code REFUND_PENDING} à {@code
   * REFUNDED}.
   */
  @PostMapping("/{paymentId}/complete-refund-stub")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void completeRefundStub(
      @PathVariable long paymentId, @AuthenticationPrincipal UserPrincipal principal) {
    adminRefundCompletionService.completeStubRefund(paymentId, principal.getId());
  }
}
