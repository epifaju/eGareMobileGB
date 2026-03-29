package com.garemobilegb.audit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "admin_audit_log")
public class AdminAuditLogEntry {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Instant createdAt = Instant.now();

  @Column(nullable = false)
  private long actorUserId;

  @Column(nullable = false, length = 64)
  private String action;

  @Column(nullable = false, length = 64)
  private String entityType;

  @Column private Long entityId;

  @Column(columnDefinition = "text")
  private String detailsJson;

  protected AdminAuditLogEntry() {}

  public AdminAuditLogEntry(
      long actorUserId, String action, String entityType, Long entityId, String detailsJson) {
    this.actorUserId = actorUserId;
    this.action = action;
    this.entityType = entityType;
    this.entityId = entityId;
    this.detailsJson = detailsJson;
  }

  public Long getId() {
    return id;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public long getActorUserId() {
    return actorUserId;
  }

  public String getAction() {
    return action;
  }

  public String getEntityType() {
    return entityType;
  }

  public Long getEntityId() {
    return entityId;
  }

  public String getDetailsJson() {
    return detailsJson;
  }
}
