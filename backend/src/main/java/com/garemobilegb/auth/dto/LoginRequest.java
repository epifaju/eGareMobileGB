package com.garemobilegb.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "Numéro E.164 invalide")
        String phoneNumber,
    @NotBlank @Size(min = 1, max = 128) String password) {}
