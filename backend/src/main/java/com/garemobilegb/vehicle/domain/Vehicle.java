package com.garemobilegb.vehicle.domain;

import com.garemobilegb.station.domain.Station;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "vehicles")
public class Vehicle {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "station_id", nullable = false)
  private Station station;

  /** Immatriculation ou identifiant court affiché au voyageur. */
  @Column(nullable = false, length = 32)
  private String registrationCode;

  /** Libellé de ligne / destination (ex. Bissau → Gabú). */
  @Column(nullable = false, length = 200)
  private String routeLabel;

  @Column(nullable = false)
  private int capacity;

  @Column(nullable = false)
  private int occupiedSeats;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 8)
  private VehicleSeatLayout seatLayout = VehicleSeatLayout.L20;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private VehicleStatus status = VehicleStatus.EN_FILE;

  /** Départ prévu (optionnel, pour tri). */
  @Column private Instant departureScheduledAt;

  @Column(name = "current_latitude")
  private Double currentLatitude;

  @Column(name = "current_longitude")
  private Double currentLongitude;

  @Column(name = "location_updated_at")
  private Instant locationUpdatedAt;

  /** Tarif indicatif en XOF (nullable si non renseigné). */
  @Column(name = "fare_amount_xof")
  private Integer fareAmountXof;

  protected Vehicle() {}

  public Vehicle(
      Station station,
      String registrationCode,
      String routeLabel,
      int capacity,
      int occupiedSeats,
      VehicleSeatLayout seatLayout,
      VehicleStatus status,
      Instant departureScheduledAt,
      Double currentLatitude,
      Double currentLongitude,
      Instant locationUpdatedAt,
      Integer fareAmountXof) {
    this.station = station;
    this.registrationCode = registrationCode;
    this.routeLabel = routeLabel;
    this.capacity = capacity;
    this.occupiedSeats = occupiedSeats;
    this.seatLayout = seatLayout;
    this.status = status;
    this.departureScheduledAt = departureScheduledAt;
    this.currentLatitude = currentLatitude;
    this.currentLongitude = currentLongitude;
    this.locationUpdatedAt = locationUpdatedAt;
    this.fareAmountXof = fareAmountXof;
  }

  public Long getId() {
    return id;
  }

  public Station getStation() {
    return station;
  }

  public void setStation(Station station) {
    this.station = station;
  }

  public String getRegistrationCode() {
    return registrationCode;
  }

  public String getRouteLabel() {
    return routeLabel;
  }

  public int getCapacity() {
    return capacity;
  }

  public int getOccupiedSeats() {
    return occupiedSeats;
  }

  public void setOccupiedSeats(int occupiedSeats) {
    this.occupiedSeats = occupiedSeats;
  }

  public VehicleSeatLayout getSeatLayout() {
    return seatLayout;
  }

  public VehicleStatus getStatus() {
    return status;
  }

  public void setStatus(VehicleStatus status) {
    this.status = status;
  }

  public Instant getDepartureScheduledAt() {
    return departureScheduledAt;
  }

  public void setDepartureScheduledAt(Instant departureScheduledAt) {
    this.departureScheduledAt = departureScheduledAt;
  }

  public Integer getFareAmountXof() {
    return fareAmountXof;
  }

  public void setFareAmountXof(Integer fareAmountXof) {
    this.fareAmountXof = fareAmountXof;
  }

  public Double getCurrentLatitude() {
    return currentLatitude;
  }

  public Double getCurrentLongitude() {
    return currentLongitude;
  }

  public Instant getLocationUpdatedAt() {
    return locationUpdatedAt;
  }

  public void updateLocation(double latitude, double longitude, Instant at) {
    this.currentLatitude = latitude;
    this.currentLongitude = longitude;
    this.locationUpdatedAt = at;
  }
}
