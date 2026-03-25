package com.garemobilegb.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterPushTokenRequest(
    @NotBlank @Size(max = 512) String expoPushToken) {}
