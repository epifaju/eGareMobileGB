package com.garemobilegb.admin.dto;

import com.garemobilegb.audit.domain.AdminAuditLogEntry;
import java.time.Instant;

public record AdminAuditEntryResponse(
    long id,
    Instant createdAt,
    long actorUserId,
    String action,
    String entityType,
    Long entityId,
    String detailsJson) {

  public static AdminAuditEntryResponse from(AdminAuditLogEntry e) {
    return new AdminAuditEntryResponse(
        e.getId(),
        e.getCreatedAt(),
        e.getActorUserId(),
        e.getAction(),
        e.getEntityType(),
        e.getEntityId(),
        e.getDetailsJson());
  }
}
