package com.garemobilegb.auth.dto;

import com.garemobilegb.auth.domain.Role;
import com.garemobilegb.auth.domain.User;

public record UserRoleResponse(long id, String phoneNumber, Role role) {

  public static UserRoleResponse from(User u) {
    return new UserRoleResponse(u.getId(), u.getPhoneNumber(), u.getRole());
  }
}
