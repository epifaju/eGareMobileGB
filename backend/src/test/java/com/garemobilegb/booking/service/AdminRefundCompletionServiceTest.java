package com.garemobilegb.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.garemobilegb.auth.domain.User;
import com.garemobilegb.booking.domain.Booking;
import com.garemobilegb.booking.domain.BookingStatus;
import com.garemobilegb.booking.domain.Payment;
import com.garemobilegb.booking.domain.PaymentProvider;
import com.garemobilegb.booking.domain.PaymentStatus;
import com.garemobilegb.booking.domain.RefundAuditEventType;
import com.garemobilegb.audit.service.AdminAuditLogService;
import com.garemobilegb.booking.repository.PaymentRepository;
import com.garemobilegb.shared.exceptions.BusinessException;
import com.garemobilegb.station.domain.Station;
import com.garemobilegb.vehicle.domain.Vehicle;
import com.garemobilegb.vehicle.domain.VehicleSeatLayout;
import com.garemobilegb.vehicle.domain.VehicleStatus;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminRefundCompletionServiceTest {

  @Mock PaymentRepository paymentRepository;
  @Mock RefundAuditService refundAuditService;
  @Mock AdminAuditLogService adminAuditLogService;

  @InjectMocks AdminRefundCompletionService service;

  private Payment payment;
  private Booking booking;

  @BeforeEach
  void setUp() {
    User user = new User("+24500000001", "h", com.garemobilegb.auth.domain.Role.USER);
    ReflectionTestUtils.setField(user, "id", 1L);
    Station station = new Station("G", "V", 0, 0, null);
    ReflectionTestUtils.setField(station, "id", 1L);
    Vehicle vehicle =
        new Vehicle(
            station,
            "X",
            "L",
            20,
            5,
            VehicleSeatLayout.L20,
            VehicleStatus.EN_FILE,
            null,
            null,
            null,
            null,
            null);
    ReflectionTestUtils.setField(vehicle, "id", 9L);
    booking = new Booking(user, vehicle);
    ReflectionTestUtils.setField(booking, "id", 42L);
    booking.setStatus(BookingStatus.CANCELLED);
    payment = new Payment(booking, new BigDecimal("1000"), "XOF", PaymentProvider.ORANGE_MONEY);
    booking.attachPayment(payment);
    ReflectionTestUtils.setField(payment, "id", 100L);
    payment.setStatus(PaymentStatus.REFUND_PENDING);
    payment.setRefundAmount(new BigDecimal("800.00"));
  }

  @Test
  void completeStubRefund_marksRefundedAndAudits() {
    when(paymentRepository.findWithBookingById(100L)).thenReturn(Optional.of(payment));
    when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

    service.completeStubRefund(100L, 99L);

    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
    assertThat(payment.getRefundedAt()).isNotNull();
    assertThat(payment.getRefundProviderRef()).startsWith("admin-stub-");

    ArgumentCaptor<String> detail = ArgumentCaptor.forClass(String.class);
    verify(refundAuditService)
        .append(eq(42L), eq(99L), eq(RefundAuditEventType.ADMIN_REFUND_STUB_COMPLETED), detail.capture());
    assertThat(detail.getValue()).contains("paymentId=100");
  }

  @Test
  void completeStubRefund_rejectsWhenNotPending() {
    payment.setStatus(PaymentStatus.PAID);
    when(paymentRepository.findWithBookingById(100L)).thenReturn(Optional.of(payment));

    assertThatThrownBy(() -> service.completeStubRefund(100L, 99L))
        .isInstanceOf(BusinessException.class)
        .hasFieldOrPropertyWithValue("code", "PAYMENT_NOT_REFUND_PENDING");
  }

  @Test
  void completeStubRefund_rejectsWhenBookingNotCancelled() {
    booking.setStatus(BookingStatus.CONFIRMED);
    when(paymentRepository.findWithBookingById(100L)).thenReturn(Optional.of(payment));

    assertThatThrownBy(() -> service.completeStubRefund(100L, 99L))
        .isInstanceOf(BusinessException.class)
        .hasFieldOrPropertyWithValue("code", "BOOKING_NOT_CANCELLED");
  }
}
