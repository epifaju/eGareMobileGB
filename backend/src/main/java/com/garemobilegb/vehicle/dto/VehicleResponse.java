package com.garemobilegb.vehicle.dto;

import com.garemobilegb.vehicle.domain.Vehicle;
import com.garemobilegb.vehicle.domain.VehicleSeatLayout;
import com.garemobilegb.vehicle.domain.VehicleStatus;
import java.time.Instant;

public record VehicleResponse(
    long id,
    long stationId,
    String registrationCode,
    String routeLabel,
    int capacity,
    VehicleSeatLayout seatLayout,
    int occupiedSeats,
    int unavailableSeats,
    int availableSeats,
    VehicleStatus status,
    Instant departureScheduledAt,
    Integer estimatedWaitMinutes,
    Double currentLatitude,
    Double currentLongitude,
    Instant locationUpdatedAt,
    Integer fareAmountXof) {

  public static VehicleResponse from(Vehicle v) {
    return new VehicleResponse(
        v.getId(),
        v.getStation().getId(),
        v.getRegistrationCode(),
        v.getRouteLabel(),
        v.getCapacity(),
        v.getSeatLayout(),
        v.getOccupiedSeats(),
        v.getOccupiedSeats(),
        Math.max(0, v.getCapacity() - v.getOccupiedSeats()),
        v.getStatus(),
        v.getDepartureScheduledAt(),
        null,
        v.getCurrentLatitude(),
        v.getCurrentLongitude(),
        v.getLocationUpdatedAt(),
        v.getFareAmountXof());
  }

  public VehicleResponse withEstimatedWaitMinutes(int estimatedWaitMinutes) {
    return new VehicleResponse(
        id,
        stationId,
        registrationCode,
        routeLabel,
        capacity,
        seatLayout,
        occupiedSeats,
        unavailableSeats,
        availableSeats,
        status,
        departureScheduledAt,
        estimatedWaitMinutes,
        currentLatitude,
        currentLongitude,
        locationUpdatedAt,
        fareAmountXof);
  }

  public VehicleResponse withSeatCounters(int occupiedSeats, int unavailableSeats, int availableSeats) {
    return new VehicleResponse(
        id,
        stationId,
        registrationCode,
        routeLabel,
        capacity,
        seatLayout,
        occupiedSeats,
        unavailableSeats,
        availableSeats,
        status,
        departureScheduledAt,
        estimatedWaitMinutes,
        currentLatitude,
        currentLongitude,
        locationUpdatedAt,
        fareAmountXof);
  }
}
