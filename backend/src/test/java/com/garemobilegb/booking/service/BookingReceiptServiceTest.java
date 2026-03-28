package com.garemobilegb.booking.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.garemobilegb.auth.domain.User;
import com.garemobilegb.booking.domain.Booking;
import com.garemobilegb.booking.domain.BookingStatus;
import com.garemobilegb.booking.domain.Payment;
import com.garemobilegb.booking.domain.PaymentProvider;
import com.garemobilegb.booking.domain.PaymentStatus;
import com.garemobilegb.booking.receipt.ReceiptPdfData;
import com.garemobilegb.booking.receipt.ReceiptPdfGenerator;
import com.garemobilegb.booking.repository.BookingRepository;
import com.garemobilegb.shared.config.ReceiptProperties;
import com.garemobilegb.shared.exceptions.BusinessException;
import com.garemobilegb.shared.sms.SmsSender;
import com.garemobilegb.station.domain.Station;
import com.garemobilegb.vehicle.domain.Vehicle;
import com.garemobilegb.vehicle.domain.VehicleSeatLayout;
import com.garemobilegb.vehicle.domain.VehicleStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookingReceiptServiceTest {

  @Mock BookingRepository bookingRepository;
  @Mock ReceiptPdfGenerator receiptPdfGenerator;
  @Mock ReceiptProperties receiptProperties;
  @Mock SmsSender smsSender;

  @InjectMocks BookingReceiptService service;

  private Booking paidBooking;

  @BeforeEach
  void setUp() {
    Station station = new Station("Gare A", "Bissau", 0, 0, null);
    Vehicle vehicle =
        new Vehicle(
            station,
            "AB-123",
            "Route X",
            20,
            5,
            VehicleSeatLayout.L20,
            VehicleStatus.EN_FILE,
            Instant.parse("2026-03-30T08:00:00Z"),
            null,
            null,
            null,
            5000);
    User user = new User("+24550000000", "hash", com.garemobilegb.auth.domain.Role.USER);
    paidBooking = new Booking(user, vehicle);
    paidBooking.setStatus(BookingStatus.CONFIRMED);
    paidBooking.setSeatNumber(3);
    Payment payment =
        new Payment(paidBooking, new BigDecimal("5000"), "XOF", PaymentProvider.INTERNAL);
    payment.setStatus(PaymentStatus.PAID);
    paidBooking.attachPayment(payment);
    ReflectionTestUtils.setField(paidBooking, "id", 1L);
    when(bookingRepository.findByIdAndUser_Id(1L, 9L)).thenReturn(Optional.of(paidBooking));
    when(receiptPdfGenerator.generate(any(ReceiptPdfData.class))).thenReturn(new byte[] {1, 2, 3});
    when(receiptProperties.smsTemplateOrDefault()).thenReturn("id={bookingId} route={route}");
  }

  @Test
  void buildReceiptPdf_whenPendingPayment_throws() {
    paidBooking.getPayment().setStatus(PaymentStatus.PENDING);
    assertThatThrownBy(() -> service.buildReceiptPdf(1L, 9L))
        .isInstanceOf(BusinessException.class)
        .hasFieldOrPropertyWithValue("code", "RECEIPT_NOT_AVAILABLE");
    verify(receiptPdfGenerator, never()).generate(any());
  }

  @Test
  void buildReceiptPdf_whenPaid_returnsBytes() {
    byte[] pdf = service.buildReceiptPdf(1L, 9L);
    org.assertj.core.api.Assertions.assertThat(pdf).containsExactly(1, 2, 3);
    verify(receiptPdfGenerator).generate(any(ReceiptPdfData.class));
  }

  @Test
  void sendReceiptSms_sendsToUserPhone() {
    service.sendReceiptSms(1L, 9L);
    verify(smsSender).sendTransactionalSms(eq("+24550000000"), eq("id=1 route=Route X"));
  }

  @Test
  void sendReceiptSms_whenNotFound_throws() {
    when(bookingRepository.findByIdAndUser_Id(99L, 9L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.sendReceiptSms(99L, 9L))
        .isInstanceOf(BusinessException.class)
        .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    verify(smsSender, never()).sendTransactionalSms(any(), any());
  }
}
