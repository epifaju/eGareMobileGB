package com.garemobilegb.admin.dto;

import com.garemobilegb.vehicle.domain.VehicleSeatLayout;
import com.garemobilegb.vehicle.domain.VehicleStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record CreateAdminVehicleRequest(
    @NotBlank @Size(max = 32) String registrationCode,
    @NotBlank @Size(max = 200) String routeLabel,
    @NotNull VehicleSeatLayout seatLayout,
    @Min(0) int occupiedSeats,
    Integer fareAmountXof,
    Instant departureScheduledAt,
    VehicleStatus status) {}
