package com.garemobilegb.booking.service;

import com.garemobilegb.booking.domain.Booking;
import com.garemobilegb.booking.domain.BookingStatus;
import com.garemobilegb.booking.domain.PaymentStatus;
import com.garemobilegb.booking.dto.BoardingValidationResponse;
import com.garemobilegb.booking.repository.BookingRepository;
import com.garemobilegb.shared.exceptions.BusinessException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BoardingValidationService {

  private final BookingRepository bookingRepository;

  public BoardingValidationService(BookingRepository bookingRepository) {
    this.bookingRepository = bookingRepository;
  }

  /**
   * Valide un billet QR pour le véhicule indiqué (scan conducteur). Idempotent si déjà validé.
   */
  @Transactional
  public BoardingValidationResponse validateForVehicle(long vehicleId, String qrToken) {
    Booking booking =
        bookingRepository
            .findByQrToken(qrToken.trim())
            .orElseThrow(
                () ->
                    new BusinessException(
                        HttpStatus.NOT_FOUND, "QR_NOT_FOUND", "Code QR inconnu ou expiré."));

    if (!booking.getVehicle().getId().equals(vehicleId)) {
      throw new BusinessException(
          HttpStatus.CONFLICT,
          "QR_VEHICLE_MISMATCH",
          "Ce billet n’est pas valable pour ce véhicule.");
    }

    var v = booking.getVehicle();
    var st = v.getStation();
    if (v.isArchived() || (st != null && st.isArchived())) {
      throw new BusinessException(
          HttpStatus.CONFLICT, "VEHICLE_NOT_ACTIVE", "Ce véhicule n’accepte plus d’embarquement.");
    }

    if (booking.getStatus() == BookingStatus.CANCELLED) {
      throw new BusinessException(
          HttpStatus.CONFLICT, "BOOKING_CANCELLED", "Cette réservation a été annulée.");
    }

    if (booking.getStatus() == BookingStatus.EXPIRED) {
      throw new BusinessException(
          HttpStatus.CONFLICT, "BOOKING_EXPIRED", "Cette réservation a expiré.");
    }

    if (booking.getStatus() != BookingStatus.CONFIRMED) {
      throw new BusinessException(
          HttpStatus.CONFLICT,
          "BOOKING_NOT_CONFIRMED",
          "Le billet n’est pas confirmé (paiement ou statut invalide).");
    }

    var payment = booking.getPayment();
    if (payment != null && payment.getStatus() != PaymentStatus.PAID) {
      throw new BusinessException(
          HttpStatus.CONFLICT, "PAYMENT_NOT_PAID", "Le paiement de ce billet n’est pas validé.");
    }

    if (booking.getExpiresAt() != null && booking.getExpiresAt().isBefore(Instant.now())) {
      throw new BusinessException(
          HttpStatus.CONFLICT, "QR_EXPIRED", "La validité de ce billet est dépassée.");
    }

    boolean already = booking.getBoardingValidatedAt() != null;
    if (!already) {
      booking.setBoardingValidatedAt(Instant.now());
      bookingRepository.save(booking);
    }

    return BoardingValidationResponse.from(booking, already);
  }
}
