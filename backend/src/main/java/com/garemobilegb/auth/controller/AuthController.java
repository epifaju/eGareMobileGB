package com.garemobilegb.auth.controller;

import com.garemobilegb.auth.dto.LoginRequest;
import com.garemobilegb.auth.dto.OtpRequest;
import com.garemobilegb.auth.dto.OtpResponse;
import com.garemobilegb.auth.dto.RefreshRequest;
import com.garemobilegb.auth.dto.RegisterRequest;
import com.garemobilegb.auth.dto.TokenResponse;
import com.garemobilegb.auth.service.AuthService;
import com.garemobilegb.auth.service.OtpService;
import com.garemobilegb.shared.config.AuthDevProperties;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;
  private final OtpService otpService;
  private final AuthDevProperties authDevProperties;

  public AuthController(
      AuthService authService, OtpService otpService, AuthDevProperties authDevProperties) {
    this.authService = authService;
    this.otpService = otpService;
    this.authDevProperties = authDevProperties;
  }

  /** Demande un OTP SMS (stocké Redis) avant l’inscription. */
  @PostMapping("/otp")
  @ResponseStatus(HttpStatus.OK)
  public OtpResponse requestOtp(@Valid @RequestBody OtpRequest request) {
    return otpService.requestOtp(request.phoneNumber(), authDevProperties.exposeOtpInResponse());
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public TokenResponse register(@Valid @RequestBody RegisterRequest request) {
    return authService.register(request);
  }

  @PostMapping("/login")
  @ResponseStatus(HttpStatus.OK)
  public TokenResponse login(@Valid @RequestBody LoginRequest request) {
    return authService.login(request);
  }

  @PostMapping("/refresh")
  @ResponseStatus(HttpStatus.OK)
  public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
    return authService.refresh(request);
  }
}
