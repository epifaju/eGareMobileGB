package com.garemobilegb.booking.controller;

import com.garemobilegb.booking.dto.BookingResponse;
import com.garemobilegb.booking.dto.PaymentConfirmRequest;
import com.garemobilegb.booking.dto.PaymentInitiateRequest;
import com.garemobilegb.booking.dto.PaymentInitiateResponse;
import com.garemobilegb.booking.service.BookingReceiptService;
import com.garemobilegb.booking.service.BookingService;
import com.garemobilegb.shared.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BookingController {

  private final BookingService bookingService;
  private final BookingReceiptService bookingReceiptService;

  public BookingController(
      BookingService bookingService, BookingReceiptService bookingReceiptService) {
    this.bookingService = bookingService;
    this.bookingReceiptService = bookingReceiptService;
  }

  @GetMapping("/me/bookings")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  public Page<BookingResponse> listMineBookings(
      @AuthenticationPrincipal UserPrincipal principal,
      @RequestParam(defaultValue = "true") boolean includeCancelled,
      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return bookingService.listForUser(principal.getId(), includeCancelled, pageable);
  }

  /**
   * @deprecated Remplacé par {@code GET /api/me/bookings} (PRD §5.5). Conservé pour compatibilité
   *     clients existants.
   */
  @Deprecated
  @GetMapping("/me/reservations")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  public Page<BookingResponse> listMineReservationsLegacy(
      @AuthenticationPrincipal UserPrincipal principal,
      @RequestParam(defaultValue = "true") boolean includeCancelled,
      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return bookingService.listForUser(principal.getId(), includeCancelled, pageable);
  }

  @GetMapping("/bookings/{id}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  public BookingResponse getOne(
      @PathVariable long id, @AuthenticationPrincipal UserPrincipal principal) {
    return bookingService.getByIdForUser(id, principal.getId());
  }

  @DeleteMapping("/bookings/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('USER')")
  public void cancelBooking(
      @PathVariable long id, @AuthenticationPrincipal UserPrincipal principal) {
    bookingService.cancelBooking(id, principal.getId());
  }

  /**
   * @deprecated Remplacé par {@code DELETE /api/bookings/{id}}.
   */
  @Deprecated
  @DeleteMapping("/reservations/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('USER')")
  public void cancelReservationLegacy(
      @PathVariable long id, @AuthenticationPrincipal UserPrincipal principal) {
    bookingService.cancelBooking(id, principal.getId());
  }

  /**
   * Confirmation de paiement (Phase 0 : stub interne si flux sans passerelle). Passerelles Orange
   * Money / Wave : à brancher ultérieurement.
   */
  @PostMapping("/payments/{bookingId}/confirm")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('USER')")
  public void confirmPayment(
      @PathVariable long bookingId,
      @AuthenticationPrincipal UserPrincipal principal,
      @RequestBody(required = false) PaymentConfirmRequest body) {
    bookingService.confirmPayment(
        bookingId, principal.getId(), body != null ? body : new PaymentConfirmRequest(null));
  }

  /**
   * Démarre un paiement (URL sandbox ou redirection passerelle). Phase 4 PRD : Orange / Wave /
   * MTN.
   */
  @PostMapping("/bookings/{bookingId}/payment/initiate")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  public PaymentInitiateResponse initiatePayment(
      @PathVariable long bookingId,
      @AuthenticationPrincipal UserPrincipal principal,
      @Valid @RequestBody PaymentInitiateRequest body,
      @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
    return bookingService.initiatePayment(bookingId, principal.getId(), body, idempotencyKey);
  }

  /** Phase 5 — reçu PDF (paiement confirmé ou état de remboursement lié). */
  @GetMapping(value = "/bookings/{id}/receipt", produces = MediaType.APPLICATION_PDF_VALUE)
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<byte[]> downloadReceipt(
      @PathVariable long id, @AuthenticationPrincipal UserPrincipal principal) {
    byte[] pdf = bookingReceiptService.buildReceiptPdf(id, principal.getId());
    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"recu-reservation-" + id + ".pdf\"")
        .contentType(MediaType.APPLICATION_PDF)
        .body(pdf);
  }

  /** Phase 5 — rappel par SMS (contenu configurable : {@code app.booking.receipt.sms-template}). */
  @PostMapping("/bookings/{id}/receipt/send-sms")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('USER')")
  public void sendReceiptSms(
      @PathVariable long id, @AuthenticationPrincipal UserPrincipal principal) {
    bookingReceiptService.sendReceiptSms(id, principal.getId());
  }
}
