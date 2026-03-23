package com.garemobilegb.auth.dto;

public record OtpResponse(String message, Integer expiresInSeconds, String debugOtp) {}
