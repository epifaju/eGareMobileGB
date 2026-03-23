package com.garemobilegb.vehicle.controller;

import com.garemobilegb.booking.service.BookingService;
import com.garemobilegb.shared.security.UserPrincipal;
import com.garemobilegb.vehicle.dto.ReserveSeatRequest;
import com.garemobilegb.vehicle.dto.VehicleResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleReservationController {

  private final BookingService bookingService;

  public VehicleReservationController(BookingService bookingService) {
    this.bookingService = bookingService;
  }

  /** Réserve une place (voyageur authentifié, rôle USER). */
  @PostMapping("/{id}/reserve-seat")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  public VehicleResponse reserveSeat(
      @PathVariable long id,
      @AuthenticationPrincipal UserPrincipal principal,
      @RequestBody(required = false) ReserveSeatRequest request) {
    Integer requestedSeat = request != null ? request.seatNumber() : null;
    return bookingService.reserveSeat(id, principal.getId(), requestedSeat);
  }
}
