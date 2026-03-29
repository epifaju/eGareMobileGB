package com.garemobilegb.admin.dto;

import com.garemobilegb.auth.domain.User;
import java.time.Instant;

public record AdminUserSummaryResponse(long id, String phoneNumber, String role, Instant createdAt) {

  public static AdminUserSummaryResponse from(User u) {
    return new AdminUserSummaryResponse(
        u.getId(), u.getPhoneNumber(), u.getRole().name(), u.getCreatedAt());
  }
}
