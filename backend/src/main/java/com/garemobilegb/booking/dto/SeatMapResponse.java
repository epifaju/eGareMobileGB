package com.garemobilegb.booking.dto;

import com.garemobilegb.vehicle.domain.VehicleSeatLayout;
import java.util.List;

public record SeatMapResponse(
    long vehicleId,
    VehicleSeatLayout layout,
    int rows,
    int columns,
    int capacity,
    int occupiedSeats,
    List<Integer> unavailableSeats,
    List<Integer> availableSeats,
    List<SeatCellResponse> cells) {}
