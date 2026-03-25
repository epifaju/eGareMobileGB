package com.garemobilegb.booking.domain;

import com.garemobilegb.auth.domain.User;
import com.garemobilegb.vehicle.domain.Vehicle;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "bookings")
public class Booking {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "vehicle_id", nullable = false)
  private Vehicle vehicle;

  @Column(nullable = false)
  private Instant createdAt = Instant.now();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 24)
  private BookingStatus status = BookingStatus.PENDING_PAYMENT;

  @Column(name = "seat_number")
  private Integer seatNumber;

  /** JWT compact (Phase 2) ou ancien jeton hex ; JWT peut dépasser 512 caractères. */
  @Column(name = "qr_token", length = 2048)
  private String qrToken;

  @Column(name = "expires_at")
  private Instant expiresAt;

  /** Premier scan QR valide à l’embarquement (conducteur). */
  @Column(name = "boarding_validated_at")
  private Instant boardingValidatedAt;

  @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
  private Payment payment;

  protected Booking() {}

  public Booking(User user, Vehicle vehicle) {
    this.user = user;
    this.vehicle = vehicle;
  }

  public void attachPayment(Payment payment) {
    this.payment = payment;
    payment.setBooking(this);
  }

  public Long getId() {
    return id;
  }

  public User getUser() {
    return user;
  }

  public Vehicle getVehicle() {
    return vehicle;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public BookingStatus getStatus() {
    return status;
  }

  public void setStatus(BookingStatus status) {
    this.status = status;
  }

  public Integer getSeatNumber() {
    return seatNumber;
  }

  public void setSeatNumber(Integer seatNumber) {
    this.seatNumber = seatNumber;
  }

  public String getQrToken() {
    return qrToken;
  }

  public void setQrToken(String qrToken) {
    this.qrToken = qrToken;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(Instant expiresAt) {
    this.expiresAt = expiresAt;
  }

  public Instant getBoardingValidatedAt() {
    return boardingValidatedAt;
  }

  public void setBoardingValidatedAt(Instant boardingValidatedAt) {
    this.boardingValidatedAt = boardingValidatedAt;
  }

  public Payment getPayment() {
    return payment;
  }

  public void setPayment(Payment payment) {
    this.payment = payment;
  }
}
