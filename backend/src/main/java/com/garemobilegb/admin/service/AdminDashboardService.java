package com.garemobilegb.admin.service;

import com.garemobilegb.admin.dto.AdminDashboardResponse;
import com.garemobilegb.auth.domain.Role;
import com.garemobilegb.auth.repository.UserRepository;
import com.garemobilegb.booking.repository.BookingRepository;
import com.garemobilegb.station.repository.StationRepository;
import com.garemobilegb.vehicle.repository.VehicleRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminDashboardService {

  private final UserRepository userRepository;
  private final StationRepository stationRepository;
  private final VehicleRepository vehicleRepository;
  private final BookingRepository bookingRepository;

  public AdminDashboardService(
      UserRepository userRepository,
      StationRepository stationRepository,
      VehicleRepository vehicleRepository,
      BookingRepository bookingRepository) {
    this.userRepository = userRepository;
    this.stationRepository = stationRepository;
    this.vehicleRepository = vehicleRepository;
    this.bookingRepository = bookingRepository;
  }

  @Transactional(readOnly = true)
  public AdminDashboardResponse snapshot() {
    Map<String, Long> usersByRole = new HashMap<>();
    for (Role r : Role.values()) {
      usersByRole.put(r.name(), userRepository.countByRole(r));
    }
    long totalUsers = userRepository.count();

    Map<String, Long> byStatus = new HashMap<>();
    for (var row : vehicleRepository.countActiveVehiclesGroupedByStatus()) {
      if (row.status() != null && row.count() != null) {
        byStatus.put(row.status().name(), row.count());
      }
    }

    Instant dayStart = Instant.now().truncatedTo(ChronoUnit.DAYS);
    Instant dayEnd = dayStart.plus(1, ChronoUnit.DAYS);
    long bookingsToday = bookingRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(dayStart, dayEnd);

    return new AdminDashboardResponse(
        totalUsers,
        usersByRole,
        stationRepository.countByArchivedFalse(),
        stationRepository.countByArchivedTrue(),
        vehicleRepository.countByArchivedFalse(),
        vehicleRepository.countByArchivedTrue(),
        byStatus,
        bookingsToday);
  }
}
