package com.garemobilegb.vehicle.controller;

import com.garemobilegb.booking.dto.SeatMapResponse;
import com.garemobilegb.booking.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleSeatMapController {

  private final BookingService bookingService;

  public VehicleSeatMapController(BookingService bookingService) {
    this.bookingService = bookingService;
  }

  @GetMapping("/{id}/seat-map")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  public SeatMapResponse getSeatMap(@PathVariable long id) {
    return bookingService.getSeatMap(id);
  }
}
