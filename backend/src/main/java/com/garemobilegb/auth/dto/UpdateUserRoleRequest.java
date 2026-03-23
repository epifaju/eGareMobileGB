package com.garemobilegb.auth.dto;

import com.garemobilegb.auth.domain.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(@NotNull Role role) {}
