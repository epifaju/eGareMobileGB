package com.garemobilegb.station.dto;

import java.time.Instant;

/**
 * DTO compact pour la vue "réseau faible".
 *
 * <p>Conçu pour limiter la taille JSON (évite coordonnées/description/champs non essentiels).
 */
public record LowBandwidthStationResponse(
    long id,
    String name,
    String city,
    long activeVehicles,
    Instant nextDepartureAt,
    Integer minFareXof) {}
