package com.garemobilegb.booking.repository;

import com.garemobilegb.booking.domain.Booking;
import com.garemobilegb.booking.domain.BookingStatus;
import com.garemobilegb.booking.dto.RevenueAggregateRow;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {

  interface VehicleSeatCounters {
    long getVehicleId();

    long getConfirmedSeats();

    long getBlockedSeats();
  }

  boolean existsByUser_IdAndVehicle_IdAndStatusIn(
      long userId, long vehicleId, Collection<BookingStatus> statuses);

  boolean existsByVehicle_IdAndSeatNumberAndStatusIn(
      long vehicleId, int seatNumber, Collection<BookingStatus> statuses);

  @Query(
      "SELECT b.seatNumber FROM Booking b WHERE b.vehicle.id = :vehicleId AND b.status IN :statuses AND b.seatNumber IS NOT NULL")
  List<Integer> findSeatNumbersByVehicleIdAndStatusIn(
      @Param("vehicleId") long vehicleId, @Param("statuses") Collection<BookingStatus> statuses);

  @Query(
      "SELECT b.vehicle.id AS vehicleId, "
          + "COUNT(DISTINCT CASE WHEN b.status = :confirmedStatus THEN b.seatNumber ELSE null END) AS confirmedSeats, "
          + "COUNT(DISTINCT CASE WHEN b.status IN :blockingStatuses THEN b.seatNumber ELSE null END) AS blockedSeats "
          + "FROM Booking b "
          + "WHERE b.vehicle.id IN :vehicleIds AND b.seatNumber IS NOT NULL "
          + "GROUP BY b.vehicle.id")
  List<VehicleSeatCounters> aggregateSeatCountersByVehicleIds(
      @Param("vehicleIds") Collection<Long> vehicleIds,
      @Param("confirmedStatus") BookingStatus confirmedStatus,
      @Param("blockingStatuses") Collection<BookingStatus> blockingStatuses);

  @EntityGraph(attributePaths = {"vehicle", "vehicle.station", "payment"})
  Page<Booking> findByUser_IdAndStatusInOrderByCreatedAtDesc(
      long userId, Collection<BookingStatus> statuses, Pageable pageable);

  @EntityGraph(attributePaths = {"vehicle", "vehicle.station", "payment"})
  Page<Booking> findByUser_IdOrderByCreatedAtDesc(long userId, Pageable pageable);

  @EntityGraph(attributePaths = {"vehicle", "vehicle.station", "payment"})
  Optional<Booking> findByIdAndUser_Id(long id, long userId);

  @EntityGraph(attributePaths = {"vehicle", "vehicle.station", "payment"})
  Optional<Booking> findByQrToken(String qrToken);

  @EntityGraph(attributePaths = {"user", "payment", "vehicle"})
  @Query(
      "SELECT b FROM Booking b WHERE b.vehicle.id = :vehicleId AND b.status IN :statuses "
          + "ORDER BY CASE WHEN b.seatNumber IS NULL THEN 1 ELSE 0 END ASC, b.seatNumber ASC")
  List<Booking> findManifestForVehicle(
      @Param("vehicleId") long vehicleId, @Param("statuses") Collection<BookingStatus> statuses);

  @Query(
      "SELECT new com.garemobilegb.booking.dto.RevenueAggregateRow("
          + "COALESCE(SUM(p.amount), 0), COUNT(b.id)) FROM Booking b JOIN b.payment p "
          + "WHERE b.vehicle.id = :vehicleId AND p.status = :paid AND b.status = :confirmed "
          + "AND b.createdAt >= :from AND b.createdAt <= :to")
  RevenueAggregateRow sumPaidRevenueForVehicle(
      @Param("vehicleId") long vehicleId,
      @Param("paid") com.garemobilegb.booking.domain.PaymentStatus paid,
      @Param("confirmed") com.garemobilegb.booking.domain.BookingStatus confirmed,
      @Param("from") java.time.Instant from,
      @Param("to") java.time.Instant to);
}
