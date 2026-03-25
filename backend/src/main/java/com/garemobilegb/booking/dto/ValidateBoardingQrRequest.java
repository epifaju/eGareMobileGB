package com.garemobilegb.booking.dto;

import jakarta.validation.constraints.NotBlank;

public record ValidateBoardingQrRequest(@NotBlank String qrToken) {}
