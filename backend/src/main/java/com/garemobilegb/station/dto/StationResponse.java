package com.garemobilegb.station.dto;

public record StationResponse(
    Long id, String name, String city, double latitude, double longitude, String description) {

  public static StationResponse from(com.garemobilegb.station.domain.Station s) {
    return new StationResponse(
        s.getId(),
        s.getName(),
        s.getCity(),
        s.getLatitude(),
        s.getLongitude(),
        s.getDescription());
  }
}
