package com.garemobilegb.booking.dto;

public record SeatCellResponse(int rowIndex, int colIndex, SeatCellType type, Integer seatNumber) {}
