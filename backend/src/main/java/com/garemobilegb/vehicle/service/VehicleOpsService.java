package com.garemobilegb.vehicle.service;

import com.garemobilegb.booking.domain.BookingStatus;
import com.garemobilegb.booking.domain.PaymentStatus;
import com.garemobilegb.booking.dto.ManifestPassengerResponse;
import com.garemobilegb.booking.dto.RevenueAggregateRow;
import com.garemobilegb.booking.dto.VehicleRevenueResponse;
import com.garemobilegb.booking.repository.BookingRepository;
import com.garemobilegb.shared.exceptions.BusinessException;
import com.garemobilegb.vehicle.repository.VehicleRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehicleOpsService {

  private static final List<BookingStatus> MANIFEST_STATUSES =
      List.of(BookingStatus.CONFIRMED, BookingStatus.PENDING_PAYMENT);

  private final BookingRepository bookingRepository;
  private final VehicleRepository vehicleRepository;

  public VehicleOpsService(BookingRepository bookingRepository, VehicleRepository vehicleRepository) {
    this.bookingRepository = bookingRepository;
    this.vehicleRepository = vehicleRepository;
  }

  @Transactional(readOnly = true)
  public List<ManifestPassengerResponse> manifestForVehicle(long vehicleId) {
    if (!vehicleRepository.existsById(vehicleId)) {
      throw new BusinessException(HttpStatus.NOT_FOUND, "VEHICLE_NOT_FOUND", "Véhicule introuvable");
    }
    return bookingRepository.findManifestForVehicle(vehicleId, MANIFEST_STATUSES).stream()
        .map(ManifestPassengerResponse::from)
        .toList();
  }

  @Transactional(readOnly = true)
  public VehicleRevenueResponse revenueForVehicle(long vehicleId, Instant from, Instant to) {
    var vehicle =
        vehicleRepository
            .findById(vehicleId)
            .orElseThrow(
                () ->
                    new BusinessException(
                        HttpStatus.NOT_FOUND, "VEHICLE_NOT_FOUND", "Véhicule introuvable"));
    Instant fromEff = from != null ? from : Instant.now().minus(30, ChronoUnit.DAYS);
    Instant toEff = to != null ? to : Instant.now();
    RevenueAggregateRow agg =
        bookingRepository.sumPaidRevenueForVehicle(
            vehicleId, PaymentStatus.PAID, BookingStatus.CONFIRMED, fromEff, toEff);
    BigDecimal total = agg.totalAmount() != null ? agg.totalAmount() : BigDecimal.ZERO;
    long count = agg.bookingCount() != null ? agg.bookingCount() : 0L;
    String currency = "XOF";
    return new VehicleRevenueResponse(
        vehicle.getId(), vehicle.getRegistrationCode(), fromEff, toEff, total, currency, count);
  }
}
