package com.garemobilegb.booking.domain;

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
@Table(name = "refund_audit_logs")
public class RefundAuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "booking_id", nullable = false)
  private long bookingId;

  @Column(name = "user_id", nullable = false)
  private long userId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 48)
  private RefundAuditEventType eventType;

  @Column(nullable = false, length = 4000)
  private String detail;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  protected RefundAuditLog() {}

  public RefundAuditLog(long bookingId, long userId, RefundAuditEventType eventType, String detail) {
    this.bookingId = bookingId;
    this.userId = userId;
    this.eventType = eventType;
    this.detail = detail;
  }

  public Long getId() {
    return id;
  }

  public long getBookingId() {
    return bookingId;
  }

  public long getUserId() {
    return userId;
  }

  public RefundAuditEventType getEventType() {
    return eventType;
  }

  public String getDetail() {
    return detail;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
