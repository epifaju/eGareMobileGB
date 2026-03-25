package com.garemobilegb.vehicle.dto;

/** Réponse {@code POST /api/vehicles/{id}/reserve-seat} — inclut l’identifiant réservation pour le flux paiement. */
public record ReserveSeatResponse(
    long bookingId, String bookingStatus, VehicleResponse vehicle) {}
