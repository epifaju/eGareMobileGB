package com.garemobilegb.admin.dto;

import com.garemobilegb.vehicle.domain.Vehicle;
import com.garemobilegb.vehicle.domain.VehicleSeatLayout;
import com.garemobilegb.vehicle.domain.VehicleStatus;
import java.time.Instant;

public record AdminVehicleResponse(
    long id,
    long stationId,
    String registrationCode,
    String routeLabel,
    int capacity,
    VehicleSeatLayout seatLayout,
    int occupiedSeats,
    VehicleStatus status,
    Instant departureScheduledAt,
    Integer fareAmountXof,
    Double currentLatitude,
    Double currentLongitude,
    Instant locationUpdatedAt,
    boolean archived) {

  public static AdminVehicleResponse from(Vehicle v) {
    return new AdminVehicleResponse(
        v.getId(),
        v.getStation().getId(),
        v.getRegistrationCode(),
        v.getRouteLabel(),
        v.getCapacity(),
        v.getSeatLayout(),
        v.getOccupiedSeats(),
        v.getStatus(),
        v.getDepartureScheduledAt(),
        v.getFareAmountXof(),
        v.getCurrentLatitude(),
        v.getCurrentLongitude(),
        v.getLocationUpdatedAt(),
        v.isArchived());
  }
}
