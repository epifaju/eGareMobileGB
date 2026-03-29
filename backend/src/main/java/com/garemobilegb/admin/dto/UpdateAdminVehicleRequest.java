package com.garemobilegb.admin.dto;

import com.garemobilegb.vehicle.domain.VehicleSeatLayout;
import com.garemobilegb.vehicle.domain.VehicleStatus;
import java.time.Instant;

/** Champs {@code null} = inchangé. */
public record UpdateAdminVehicleRequest(
    String registrationCode,
    String routeLabel,
    VehicleSeatLayout seatLayout,
    Integer occupiedSeats,
    Integer fareAmountXof,
    Instant departureScheduledAt,
    VehicleStatus status,
    Long stationId) {}
