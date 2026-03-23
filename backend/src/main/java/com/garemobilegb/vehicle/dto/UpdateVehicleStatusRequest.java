package com.garemobilegb.vehicle.dto;

import com.garemobilegb.vehicle.domain.VehicleStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateVehicleStatusRequest(@NotNull VehicleStatus status) {}
