package com.garemobilegb.notification.event;

/** Publié après changement des places occupées sur un véhicule (résa / annulation). */
public record VehicleOccupancyChangedEvent(
    long vehicleId,
    long stationId,
    String registrationCode,
    String routeLabel,
    int previousOccupiedSeats,
    int currentOccupiedSeats,
    int capacity) {}
