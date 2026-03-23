package com.garemobilegb.booking.service;

import com.garemobilegb.auth.domain.User;
import com.garemobilegb.auth.repository.UserRepository;
import com.garemobilegb.booking.domain.Booking;
import com.garemobilegb.booking.domain.BookingStatus;
import com.garemobilegb.booking.domain.Payment;
import com.garemobilegb.booking.domain.PaymentProvider;
import com.garemobilegb.booking.domain.PaymentStatus;
import com.garemobilegb.booking.dto.BookingResponse;
import com.garemobilegb.booking.dto.PaymentConfirmRequest;
import com.garemobilegb.booking.dto.SeatCellResponse;
import com.garemobilegb.booking.dto.SeatCellType;
import com.garemobilegb.booking.dto.SeatMapResponse;
import com.garemobilegb.booking.repository.BookingRepository;
import com.garemobilegb.shared.config.BookingProperties;
import com.garemobilegb.shared.exceptions.BusinessException;
import com.garemobilegb.vehicle.domain.Vehicle;
import com.garemobilegb.vehicle.domain.VehicleSeatLayout;
import com.garemobilegb.vehicle.domain.VehicleStatus;
import com.garemobilegb.vehicle.dto.VehicleResponse;
import com.garemobilegb.vehicle.repository.VehicleRepository;
import com.garemobilegb.vehicle.service.VehicleRealtimeService;
import com.garemobilegb.vehicle.service.VehicleWaitTimeEstimator;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

  private static final List<BookingStatus> BLOCKING_STATUSES =
      List.of(BookingStatus.PENDING_PAYMENT, BookingStatus.CONFIRMED);

  private static final List<BookingStatus> LIST_ACTIVE_ONLY =
      List.of(BookingStatus.CONFIRMED, BookingStatus.PENDING_PAYMENT);

  private static final long BOOKING_TTL_HOURS = 24;

  private final BookingRepository bookingRepository;
  private final UserRepository userRepository;
  private final VehicleRepository vehicleRepository;
  private final VehicleRealtimeService vehicleRealtimeService;
  private final BookingProperties bookingProperties;
  private final VehicleWaitTimeEstimator vehicleWaitTimeEstimator;

  public BookingService(
      BookingRepository bookingRepository,
      UserRepository userRepository,
      VehicleRepository vehicleRepository,
      VehicleRealtimeService vehicleRealtimeService,
      BookingProperties bookingProperties,
      VehicleWaitTimeEstimator vehicleWaitTimeEstimator) {
    this.bookingRepository = bookingRepository;
    this.userRepository = userRepository;
    this.vehicleRepository = vehicleRepository;
    this.vehicleRealtimeService = vehicleRealtimeService;
    this.bookingProperties = bookingProperties;
    this.vehicleWaitTimeEstimator = vehicleWaitTimeEstimator;
  }

  @Transactional
  public VehicleResponse reserveSeat(long vehicleId, long userId, Integer requestedSeatNumber) {
    if (bookingRepository.existsByUser_IdAndVehicle_IdAndStatusIn(
        userId, vehicleId, BLOCKING_STATUSES)) {
      throw new BusinessException(
          HttpStatus.CONFLICT,
          "BOOKING_EXISTS",
          "Vous avez déjà une réservation active ou en attente de paiement sur ce véhicule.");
    }
    User user = loadUser(userId);
    Vehicle vehicle = loadVehicleForUpdate(vehicleId);
    validateVehicleForNewBooking(vehicle);

    Booking booking = new Booking(user, vehicle);
    Payment payment =
        new Payment(booking, BigDecimal.ZERO, "XOF", PaymentProvider.INTERNAL);
    booking.attachPayment(payment);

    int resolvedSeatNumber = resolveSeatNumber(vehicle, requestedSeatNumber);
    booking.setSeatNumber(resolvedSeatNumber);

    if (bookingProperties.autoConfirmWithoutPaymentGateway()) {
      applyVehicleIncrement(vehicle);
      Vehicle savedVehicle = vehicleRepository.save(vehicle);
      finalizeConfirmedBooking(booking, payment, savedVehicle);
      bookingRepository.save(booking);
      VehicleResponse dto = toVehicleResponse(savedVehicle);
      vehicleRealtimeService.broadcastUpdate(savedVehicle.getStation().getId(), dto);
      return dto;
    }

    payment.setStatus(PaymentStatus.PENDING);
    booking.setStatus(BookingStatus.PENDING_PAYMENT);
    bookingRepository.save(booking);
    VehicleResponse dto = toVehicleResponse(vehicle);
    vehicleRealtimeService.broadcastUpdate(vehicle.getStation().getId(), dto);
    return dto;
  }

  @Transactional
  public void confirmPayment(long bookingId, long userId, PaymentConfirmRequest body) {
    Booking booking =
        bookingRepository
            .findByIdAndUser_Id(bookingId, userId)
            .orElseThrow(
                () ->
                    new BusinessException(
                        HttpStatus.NOT_FOUND, "BOOKING_NOT_FOUND", "Réservation introuvable"));
    if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
      throw new BusinessException(
          HttpStatus.CONFLICT,
          "BOOKING_NOT_PENDING_PAYMENT",
          "Cette réservation n’est pas en attente de paiement.");
    }
    Payment payment = booking.getPayment();
    if (payment == null || payment.getStatus() != PaymentStatus.PENDING) {
      throw new BusinessException(
          HttpStatus.CONFLICT, "PAYMENT_NOT_PENDING", "Paiement déjà traité ou absent.");
    }
    Vehicle vehicle = loadVehicleForUpdate(booking.getVehicle().getId());
    validateVehicleForNewBooking(vehicle);

    if (body != null && body.providerTransactionRef() != null && !body.providerTransactionRef().isBlank()) {
      payment.setProviderRef(body.providerTransactionRef().trim());
    }

    applyVehicleIncrement(vehicle);
    vehicleRepository.save(vehicle);
    finalizeConfirmedBooking(booking, payment, vehicle);
    bookingRepository.save(booking);
    VehicleResponse dto = toVehicleResponse(vehicle);
    vehicleRealtimeService.broadcastUpdate(vehicle.getStation().getId(), dto);
  }

  @Transactional
  public void cancelBooking(long bookingId, long userId) {
    Booking booking =
        bookingRepository
            .findByIdAndUser_Id(bookingId, userId)
            .orElseThrow(
                () ->
                    new BusinessException(
                        HttpStatus.NOT_FOUND, "BOOKING_NOT_FOUND", "Réservation introuvable"));
    if (booking.getStatus() == BookingStatus.CANCELLED) {
      throw new BusinessException(
          HttpStatus.CONFLICT, "BOOKING_ALREADY_CANCELLED", "Cette réservation est déjà annulée.");
    }
    if (booking.getStatus() == BookingStatus.EXPIRED) {
      throw new BusinessException(
          HttpStatus.CONFLICT, "BOOKING_EXPIRED", "Cette réservation a expiré.");
    }
    Vehicle vehicle = booking.getVehicle();
    if (vehicle.getStatus() == VehicleStatus.PARTI) {
      throw new BusinessException(
          HttpStatus.CONFLICT, "VEHICLE_DEPARTED", "Le véhicule est déjà parti.");
    }

    if (booking.getStatus() == BookingStatus.CONFIRMED) {
      int next = Math.max(0, vehicle.getOccupiedSeats() - 1);
      vehicle.setOccupiedSeats(next);
      adjustVehicleStatusAfterDecrement(vehicle);
      Payment p = booking.getPayment();
      if (p != null && p.getStatus() == PaymentStatus.PAID) {
        p.setStatus(PaymentStatus.REFUNDED);
      }
    } else if (booking.getStatus() == BookingStatus.PENDING_PAYMENT) {
      Payment p = booking.getPayment();
      if (p != null && p.getStatus() == PaymentStatus.PENDING) {
        p.setStatus(PaymentStatus.FAILED);
      }
    }

    booking.setStatus(BookingStatus.CANCELLED);
    vehicleRepository.save(vehicle);
    bookingRepository.save(booking);
    VehicleResponse dto = toVehicleResponse(vehicle);
    vehicleRealtimeService.broadcastUpdate(vehicle.getStation().getId(), dto);
  }

  @Transactional(readOnly = true)
  public Page<BookingResponse> listForUser(
      long userId, boolean includeCancelled, Pageable pageable) {
    Page<Booking> page =
        includeCancelled
            ? bookingRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable)
            : bookingRepository.findByUser_IdAndStatusInOrderByCreatedAtDesc(
                userId, LIST_ACTIVE_ONLY, pageable);
    return page.map(BookingResponse::from);
  }

  @Transactional(readOnly = true)
  public BookingResponse getByIdForUser(long bookingId, long userId) {
    Booking booking =
        bookingRepository
            .findByIdAndUser_Id(bookingId, userId)
            .orElseThrow(
                () ->
                    new BusinessException(
                        HttpStatus.NOT_FOUND, "BOOKING_NOT_FOUND", "Réservation introuvable"));
    return BookingResponse.from(booking);
  }

  @Transactional(readOnly = true)
  public SeatMapResponse getSeatMap(long vehicleId) {
    Vehicle vehicle = loadVehicle(vehicleId);
    VehicleSeatLayout layout = validateLayoutCompatibility(vehicle);
    List<Integer> unavailable =
        bookingRepository.findSeatNumbersByVehicleIdAndStatusIn(vehicleId, BLOCKING_STATUSES).stream()
            .distinct()
            .sorted()
            .toList();
    int occupiedSeats =
        (int)
            bookingRepository
                .findSeatNumbersByVehicleIdAndStatusIn(
                    vehicleId, List.of(BookingStatus.CONFIRMED))
                .stream()
                .distinct()
                .count();
    Set<Integer> unavailableSet = new HashSet<>(unavailable);
    List<Integer> available = new ArrayList<>();
    for (int seat = 1; seat <= vehicle.getCapacity(); seat++) {
      if (!unavailableSet.contains(seat)) {
        available.add(seat);
      }
    }
    List<SeatCellResponse> cells = buildSeatCells(layout, unavailableSet);
    int columns = layout.leftSeatsPerRow() + 1 + layout.rightSeatsPerRow();
    return new SeatMapResponse(
        vehicle.getId(),
        layout,
        layout.rows(),
        columns,
        vehicle.getCapacity(),
        occupiedSeats,
        unavailable,
        available,
        cells);
  }

  private User loadUser(long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(
            () ->
                new BusinessException(
                    HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Utilisateur introuvable"));
  }

  private Vehicle loadVehicle(long vehicleId) {
    return vehicleRepository
        .findById(vehicleId)
        .orElseThrow(
            () ->
                new BusinessException(
                    HttpStatus.NOT_FOUND, "VEHICLE_NOT_FOUND", "Véhicule introuvable"));
  }

  private Vehicle loadVehicleForUpdate(long vehicleId) {
    return vehicleRepository
        .findByIdForUpdate(vehicleId)
        .orElseThrow(
            () ->
                new BusinessException(
                    HttpStatus.NOT_FOUND, "VEHICLE_NOT_FOUND", "Véhicule introuvable"));
  }

  private int resolveSeatNumber(Vehicle vehicle, Integer requestedSeatNumber) {
    validateLayoutCompatibility(vehicle);
    int cap = vehicle.getCapacity();
    if (requestedSeatNumber != null) {
      if (requestedSeatNumber < 1 || requestedSeatNumber > cap) {
        throw new BusinessException(
            HttpStatus.BAD_REQUEST,
            "SEAT_NUMBER_INVALID",
            "Numéro de siège invalide pour ce véhicule.");
      }
      boolean seatTaken =
          bookingRepository.existsByVehicle_IdAndSeatNumberAndStatusIn(
              vehicle.getId(), requestedSeatNumber, BLOCKING_STATUSES);
      if (seatTaken) {
        throw new BusinessException(
            HttpStatus.CONFLICT, "SEAT_ALREADY_TAKEN", "Ce siège est déjà réservé.");
      }
      return requestedSeatNumber;
    }
    Set<Integer> taken =
        new HashSet<>(
            bookingRepository.findSeatNumbersByVehicleIdAndStatusIn(vehicle.getId(), BLOCKING_STATUSES));
    for (int seat = 1; seat <= cap; seat++) {
      if (!taken.contains(seat)) {
        return seat;
      }
    }
    throw new BusinessException(
        HttpStatus.CONFLICT, "NO_SEAT_AVAILABLE", "Aucun siège disponible sur ce véhicule.");
  }

  private VehicleSeatLayout validateLayoutCompatibility(Vehicle vehicle) {
    VehicleSeatLayout layout = vehicle.getSeatLayout();
    VehicleSeatLayout inferred = VehicleSeatLayout.fromCapacity(vehicle.getCapacity());

    if (layout == null) {
      if (inferred != null) {
        return inferred;
      }
      throw new BusinessException(
          HttpStatus.CONFLICT,
          "SEAT_LAYOUT_MISSING",
          "Configuration de sièges absente pour ce véhicule.");
    }
    if (layout.capacity() != vehicle.getCapacity()) {
      if (inferred != null) {
        // Tolère les anciennes données incohérentes: priorité à la capacité réelle du véhicule.
        return inferred;
      }
      throw new BusinessException(
          HttpStatus.CONFLICT,
          "SEAT_LAYOUT_CAPACITY_MISMATCH",
          "Le gabarit de sièges ne correspond pas à la capacité du véhicule.");
    }
    return layout;
  }

  private static List<SeatCellResponse> buildSeatCells(
      VehicleSeatLayout layout, Set<Integer> unavailableSeats) {
    List<SeatCellResponse> cells = new ArrayList<>();
    int rows = layout.rows();
    int left = layout.leftSeatsPerRow();
    int right = layout.rightSeatsPerRow();
    int columns = left + 1 + right;
    int seatNumber = 1;
    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < columns; col++) {
        if (col == left) {
          cells.add(new SeatCellResponse(row, col, SeatCellType.AISLE, null));
          continue;
        }
        boolean unavailable = unavailableSeats.contains(seatNumber);
        cells.add(
            new SeatCellResponse(
                row,
                col,
                unavailable ? SeatCellType.SEAT_UNAVAILABLE : SeatCellType.SEAT_AVAILABLE,
                seatNumber));
        seatNumber++;
      }
    }
    return cells;
  }

  private static void validateVehicleForNewBooking(Vehicle vehicle) {
    if (vehicle.getStatus() == VehicleStatus.PARTI) {
      throw new BusinessException(
          HttpStatus.CONFLICT, "VEHICLE_DEPARTED", "Ce véhicule est déjà parti.");
    }
    if (vehicle.getOccupiedSeats() >= vehicle.getCapacity()) {
      throw new BusinessException(
          HttpStatus.CONFLICT, "VEHICLE_FULL", "Plus de place disponible sur ce véhicule.");
    }
  }

  private void applyVehicleIncrement(Vehicle vehicle) {
    int nextSeat = vehicle.getOccupiedSeats() + 1;
    vehicle.setOccupiedSeats(nextSeat);
    if (vehicle.getOccupiedSeats() >= vehicle.getCapacity()) {
      vehicle.setStatus(VehicleStatus.COMPLET);
    } else if (vehicle.getStatus() == VehicleStatus.EN_FILE) {
      vehicle.setStatus(VehicleStatus.REMPLISSAGE);
    }
  }

  private void finalizeConfirmedBooking(Booking booking, Payment payment, Vehicle vehicle) {
    booking.setStatus(BookingStatus.CONFIRMED);
    payment.setStatus(PaymentStatus.PAID);
    payment.setProvider(PaymentProvider.INTERNAL);
    if (booking.getSeatNumber() == null) {
      booking.setSeatNumber(vehicle.getOccupiedSeats());
    }
    booking.setQrToken(newQrToken());
    booking.setExpiresAt(Instant.now().plus(BOOKING_TTL_HOURS, ChronoUnit.HOURS));
  }

  private static String newQrToken() {
    return UUID.randomUUID().toString().replace("-", "")
        + UUID.randomUUID().toString().replace("-", "");
  }

  private void adjustVehicleStatusAfterDecrement(Vehicle vehicle) {
    int occ = vehicle.getOccupiedSeats();
    int cap = vehicle.getCapacity();
    if (occ == 0) {
      vehicle.setStatus(VehicleStatus.EN_FILE);
    } else if (occ < cap && vehicle.getStatus() == VehicleStatus.COMPLET) {
      vehicle.setStatus(VehicleStatus.REMPLISSAGE);
    }
  }

  private VehicleResponse toVehicleResponse(Vehicle vehicle) {
    return VehicleResponse.from(vehicle)
        .withEstimatedWaitMinutes(vehicleWaitTimeEstimator.estimateMinutes(vehicle, Instant.now()));
  }
}
