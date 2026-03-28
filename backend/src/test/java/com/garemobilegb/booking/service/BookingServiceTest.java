package com.garemobilegb.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.garemobilegb.auth.domain.Role;
import com.garemobilegb.auth.domain.User;
import com.garemobilegb.auth.repository.UserRepository;
import com.garemobilegb.booking.domain.Booking;
import com.garemobilegb.booking.repository.BookingRepository;
import com.garemobilegb.booking.domain.BookingStatus;
import com.garemobilegb.booking.payment.MobileMoneyOrchestrationService;
import com.garemobilegb.booking.service.BookingCancellationPolicy;
import com.garemobilegb.booking.service.RefundAuditService;
import com.garemobilegb.booking.service.RefundOrchestrationService;
import com.garemobilegb.shared.config.BookingProperties;
import com.garemobilegb.shared.config.RefundProperties;
import com.garemobilegb.shared.exceptions.BusinessException;
import com.garemobilegb.station.domain.Station;
import com.garemobilegb.vehicle.domain.Vehicle;
import com.garemobilegb.vehicle.domain.VehicleSeatLayout;
import com.garemobilegb.vehicle.domain.VehicleStatus;
import com.garemobilegb.vehicle.repository.VehicleRepository;
import com.garemobilegb.vehicle.service.VehicleRealtimeService;
import com.garemobilegb.vehicle.service.VehicleWaitTimeEstimator;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.ApplicationEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

  @Mock BookingRepository bookingRepository;
  @Mock UserRepository userRepository;
  @Mock VehicleRepository vehicleRepository;
  @Mock VehicleRealtimeService vehicleRealtimeService;
  @Mock BookingProperties bookingProperties;
  @Mock VehicleWaitTimeEstimator vehicleWaitTimeEstimator;
  @Mock MobileMoneyOrchestrationService mobileMoneyOrchestrationService;
  @Mock ApplicationEventPublisher eventPublisher;
  @Mock BoardingQrJwtService boardingQrJwtService;
  @Mock BookingCancellationPolicy bookingCancellationPolicy;
  @Mock RefundOrchestrationService refundOrchestrationService;
  @Mock RefundAuditService refundAuditService;
  @Mock RefundProperties refundProperties;

  @InjectMocks BookingService bookingService;

  private User user;
  private Station station;
  private Vehicle vehicle;

  @BeforeEach
  void setUp() {
    user = new User("+24500000099", "hash", Role.USER);
    ReflectionTestUtils.setField(user, "id", 2L);

    station = new Station("Gare A", "Ville", 0, 0, null);
    ReflectionTestUtils.setField(station, "id", 10L);

    vehicle =
        new Vehicle(
            station,
            "AB-01",
            "Ligne 1",
            20,
            0,
            VehicleSeatLayout.L20,
            VehicleStatus.EN_FILE,
            null,
            null,
            null,
            null,
            null);
    ReflectionTestUtils.setField(vehicle, "id", 1L);
  }

  @Test
  void reserveSeat_conflictWhenDuplicateBlockingBooking() {
    when(bookingRepository.existsByUser_IdAndVehicle_IdAndStatusIn(eq(2L), eq(1L), any()))
        .thenReturn(true);

    Throwable thrown = catchThrowable(() -> bookingService.reserveSeat(1L, 2L, null));
    assertThat(thrown).isInstanceOf(BusinessException.class);
    assertThat(((BusinessException) thrown).getCode()).isEqualTo("BOOKING_EXISTS");

    verify(userRepository, never()).findById(any());
  }

  @Test
  void reserveSeat_autoConfirm_incrementsVehicleAndSavesBooking() {
    when(bookingRepository.existsByUser_IdAndVehicle_IdAndStatusIn(eq(2L), eq(1L), any()))
        .thenReturn(false);
    when(userRepository.findById(2L)).thenReturn(Optional.of(user));
    when(vehicleRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(vehicle));
    when(bookingProperties.autoConfirmWithoutPaymentGateway()).thenReturn(true);
    when(vehicleWaitTimeEstimator.estimateMinutes(any(), any())).thenReturn(5);
    when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));
    when(bookingRepository.save(any(Booking.class)))
        .thenAnswer(
            inv -> {
              Booking b = inv.getArgument(0);
              ReflectionTestUtils.setField(b, "id", 99L);
              return b;
            });
    when(boardingQrJwtService.createBoardingQrToken(any(Booking.class)))
        .thenReturn("eyJhbGciOiJIUzI1NiJ9.fake");

    bookingService.reserveSeat(1L, 2L, 3);

    ArgumentCaptor<Vehicle> vehicleCaptor = ArgumentCaptor.forClass(Vehicle.class);
    verify(vehicleRepository).save(vehicleCaptor.capture());
    assertThat(vehicleCaptor.getValue().getOccupiedSeats()).isEqualTo(1);

    verify(bookingRepository, times(2)).save(any(Booking.class));
    verify(boardingQrJwtService).createBoardingQrToken(any(Booking.class));
    verify(vehicleRealtimeService).broadcastUpdate(eq(10L), any());
  }

  @Test
  void reserveSeat_withoutAutoConfirm_returnsPendingPaymentAndDoesNotIncrementVehicle() {
    when(bookingRepository.existsByUser_IdAndVehicle_IdAndStatusIn(eq(2L), eq(1L), any()))
        .thenReturn(false);
    when(userRepository.findById(2L)).thenReturn(Optional.of(user));
    when(vehicleRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(vehicle));
    when(bookingProperties.autoConfirmWithoutPaymentGateway()).thenReturn(false);
    when(bookingRepository.save(any(Booking.class)))
        .thenAnswer(
            inv -> {
              Booking b = inv.getArgument(0);
              ReflectionTestUtils.setField(b, "id", 100L);
              return b;
            });

    var res = bookingService.reserveSeat(1L, 2L, 3);

    assertThat(res.bookingStatus()).isEqualTo(BookingStatus.PENDING_PAYMENT.name());
    verify(vehicleRepository, never()).save(any(Vehicle.class));
    verify(boardingQrJwtService, never()).createBoardingQrToken(any(Booking.class));
    verify(vehicleRealtimeService).broadcastUpdate(eq(10L), any());
  }

  @Test
  void reserveSeat_conflictWhenRequestedSeatAlreadyTaken() {
    when(bookingRepository.existsByUser_IdAndVehicle_IdAndStatusIn(eq(2L), eq(1L), any()))
        .thenReturn(false);
    when(userRepository.findById(2L)).thenReturn(Optional.of(user));
    when(vehicleRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(vehicle));
    when(bookingRepository.existsByVehicle_IdAndSeatNumberAndStatusIn(eq(1L), eq(4), any()))
        .thenReturn(true);

    Throwable thrown = catchThrowable(() -> bookingService.reserveSeat(1L, 2L, 4));
    assertThat(thrown).isInstanceOf(BusinessException.class);
    assertThat(((BusinessException) thrown).getCode()).isEqualTo("SEAT_ALREADY_TAKEN");
  }

  @Test
  void getSeatMap_returnsAvailableAndUnavailableSeats() {
    when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
    when(
            bookingRepository.findSeatNumbersByVehicleIdAndStatusIn(
                eq(1L),
                argThat(
                    statuses ->
                        statuses.size() == 1 && statuses.contains(BookingStatus.CONFIRMED))))
        .thenReturn(java.util.List.of(2));
    when(
            bookingRepository.findSeatNumbersByVehicleIdAndStatusIn(
                eq(1L),
                argThat(
                    statuses ->
                        statuses.size() == 2
                            && statuses.contains(BookingStatus.CONFIRMED)
                            && statuses.contains(BookingStatus.PENDING_PAYMENT))))
        .thenReturn(java.util.List.of(2, 5, 8));

    var seatMap = bookingService.getSeatMap(1L);

    assertThat(seatMap.layout()).isEqualTo(VehicleSeatLayout.L20);
    assertThat(seatMap.occupiedSeats()).isEqualTo(1);
    assertThat(seatMap.unavailableSeats()).containsExactly(2, 5, 8);
    assertThat(seatMap.availableSeats()).doesNotContain(2, 5, 8);
    assertThat(seatMap.cells()).isNotEmpty();
  }

  @Test
  void getSeatMap_fallsBackToLayoutInferredFromCapacityWhenDataIsInconsistent() {
    Vehicle inconsistentVehicle =
        new Vehicle(
            station,
            "AB-99",
            "Ligne 45",
            45,
            0,
            VehicleSeatLayout.L20,
            VehicleStatus.EN_FILE,
            null,
            null,
            null,
            null,
            null);
    ReflectionTestUtils.setField(inconsistentVehicle, "id", 1L);
    when(vehicleRepository.findById(1L)).thenReturn(Optional.of(inconsistentVehicle));
    when(
            bookingRepository.findSeatNumbersByVehicleIdAndStatusIn(
                eq(1L),
                argThat(
                    statuses ->
                        statuses.size() == 1 && statuses.contains(BookingStatus.CONFIRMED))))
        .thenReturn(java.util.List.of(2));
    when(
            bookingRepository.findSeatNumbersByVehicleIdAndStatusIn(
                eq(1L),
                argThat(
                    statuses ->
                        statuses.size() == 2
                            && statuses.contains(BookingStatus.CONFIRMED)
                            && statuses.contains(BookingStatus.PENDING_PAYMENT))))
        .thenReturn(java.util.List.of(2, 5, 8));

    var seatMap = bookingService.getSeatMap(1L);

    assertThat(seatMap.layout()).isEqualTo(VehicleSeatLayout.L45);
    assertThat(seatMap.capacity()).isEqualTo(45);
    assertThat(seatMap.occupiedSeats()).isEqualTo(1);
    assertThat(seatMap.cells()).isNotEmpty();
  }
}
