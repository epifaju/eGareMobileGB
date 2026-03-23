package com.garemobilegb.vehicle.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "vehicle_wait_observations")
public class VehicleWaitObservation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long stationId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private VehicleStatus status;

  @Column(nullable = false)
  private int fillBucket;

  @Column(nullable = false)
  private int hourBucket;

  @Column(nullable = false)
  private int dayOfWeek;

  @Column(nullable = false)
  private int observedWaitMinutes;

  @Column(nullable = false)
  private Instant observedAt;

  protected VehicleWaitObservation() {}

  public VehicleWaitObservation(
      Long stationId,
      VehicleStatus status,
      int fillBucket,
      int hourBucket,
      int dayOfWeek,
      int observedWaitMinutes,
      Instant observedAt) {
    this.stationId = stationId;
    this.status = status;
    this.fillBucket = fillBucket;
    this.hourBucket = hourBucket;
    this.dayOfWeek = dayOfWeek;
    this.observedWaitMinutes = observedWaitMinutes;
    this.observedAt = observedAt;
  }

  public Long getId() {
    return id;
  }
}
