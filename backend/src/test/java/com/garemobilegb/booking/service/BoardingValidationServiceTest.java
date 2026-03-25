package com.garemobilegb.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.garemobilegb.booking.domain.Booking;
import com.garemobilegb.booking.domain.BookingStatus;
import com.garemobilegb.booking.domain.Payment;
import com.garemobilegb.booking.domain.PaymentStatus;
import com.garemobilegb.booking.repository.BookingRepository;
import com.garemobilegb.shared.exceptions.BusinessException;
import com.garemobilegb.vehicle.domain.Vehicle;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BoardingValidationServiceTest {

  private static final String QR =
      "tokentokentokentokentokentokentokentokentokentokentokentokentokentok";

  @Mock BookingRepository bookingRepository;
  @Mock Booking booking;
  @Mock Payment payment;
  @Mock Vehicle vehicle;

  @InjectMocks BoardingValidationService boardingValidationService;

  @Test
  void validateFirstScan_setsBoardingValidatedAt() {
    stubConfirmedBooking();
    when(bookingRepository.findByQrToken(QR)).thenReturn(Optional.of(booking));

    var validatedHolder = new AtomicReference<Instant>();
    doAnswer(
            inv -> {
              validatedHolder.set(inv.getArgument(0));
              return null;
            })
        .when(booking)
        .setBoardingValidatedAt(any());
    when(booking.getBoardingValidatedAt()).thenAnswer(inv -> validatedHolder.get());

    when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

    var res = boardingValidationService.validateForVehicle(5L, QR);

    assertThat(res.alreadyValidated()).isFalse();
    assertThat(res.validatedAt()).isNotNull();
    assertThat(res.seatNumber()).isEqualTo(7);
    verify(bookingRepository).save(booking);
  }

  @Test
  void validateSecondScan_idempotent() {
    stubConfirmedBooking();
    var first = Instant.parse("2025-01-01T12:00:00Z");
    when(bookingRepository.findByQrToken(QR)).thenReturn(Optional.of(booking));
    when(booking.getBoardingValidatedAt()).thenReturn(first);

    var res = boardingValidationService.validateForVehicle(5L, QR);

    assertThat(res.alreadyValidated()).isTrue();
    assertThat(res.validatedAt()).isEqualTo(first);
    verify(bookingRepository, never()).save(any());
  }

  @Test
  void wrongVehicle_throws() {
    when(bookingRepository.findByQrToken(QR)).thenReturn(Optional.of(booking));
    when(booking.getVehicle()).thenReturn(vehicle);
    when(vehicle.getId()).thenReturn(5L);

    assertThatThrownBy(() -> boardingValidationService.validateForVehicle(999L, QR))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo("QR_VEHICLE_MISMATCH");
  }

  @Test
  void unknownQr_throws() {
    when(bookingRepository.findByQrToken("x")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> boardingValidationService.validateForVehicle(5L, "x"))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo("QR_NOT_FOUND");
  }

  private void stubConfirmedBooking() {
    when(booking.getId()).thenReturn(99L);
    when(booking.getVehicle()).thenReturn(vehicle);
    when(vehicle.getId()).thenReturn(5L);
    when(vehicle.getRegistrationCode()).thenReturn("AB-123-CD");
    when(vehicle.getRouteLabel()).thenReturn("Route A");
    when(booking.getStatus()).thenReturn(BookingStatus.CONFIRMED);
    when(booking.getSeatNumber()).thenReturn(7);
    when(booking.getExpiresAt()).thenReturn(Instant.now().plusSeconds(3600));
    when(booking.getPayment()).thenReturn(payment);
    when(payment.getStatus()).thenReturn(PaymentStatus.PAID);
  }
}
