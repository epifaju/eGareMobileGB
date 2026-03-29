package com.garemobilegb.admin.dto;

import com.garemobilegb.station.domain.Station;

public record AdminStationResponse(
    long id,
    String name,
    String city,
    double latitude,
    double longitude,
    String description,
    boolean archived) {

  public static AdminStationResponse from(Station s) {
    return new AdminStationResponse(
        s.getId(),
        s.getName(),
        s.getCity(),
        s.getLatitude(),
        s.getLongitude(),
        s.getDescription(),
        s.isArchived());
  }
}
