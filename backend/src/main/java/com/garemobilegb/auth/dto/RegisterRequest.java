package com.garemobilegb.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "Numéro E.164 invalide")
        String phoneNumber,
    @NotBlank @Size(min = 8, max = 128) String password,
    @NotBlank @Pattern(regexp = "^\\d{6}$", message = "OTP à 6 chiffres requis") String otp,
    /** Si true : demande un compte conducteur (refusé si {@code app.auth.allow-driver-self-registration=false}). */
    Boolean registerAsDriver) {}
