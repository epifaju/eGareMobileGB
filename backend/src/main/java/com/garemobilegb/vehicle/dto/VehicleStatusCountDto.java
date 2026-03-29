package com.garemobilegb.vehicle.dto;

import com.garemobilegb.vehicle.domain.VehicleStatus;

/** Agrégat dashboard : véhicules actifs par statut. */
public record VehicleStatusCountDto(VehicleStatus status, Long count) {}
