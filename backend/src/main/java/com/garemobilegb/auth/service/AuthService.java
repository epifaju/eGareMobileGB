package com.garemobilegb.auth.service;

import com.garemobilegb.auth.domain.Role;
import com.garemobilegb.auth.domain.User;
import com.garemobilegb.auth.dto.LoginRequest;
import com.garemobilegb.auth.dto.RefreshRequest;
import com.garemobilegb.auth.dto.RegisterRequest;
import com.garemobilegb.auth.dto.TokenResponse;
import com.garemobilegb.auth.repository.UserRepository;
import com.garemobilegb.shared.config.AuthDevProperties;
import com.garemobilegb.shared.config.JwtProperties;
import com.garemobilegb.shared.exceptions.BusinessException;
import com.garemobilegb.shared.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final JwtProperties jwtProperties;
  private final OtpService otpService;
  private final AuthDevProperties authProperties;

  public AuthService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService,
      JwtProperties jwtProperties,
      OtpService otpService,
      AuthDevProperties authProperties) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.jwtProperties = jwtProperties;
    this.otpService = otpService;
    this.authProperties = authProperties;
  }

  @Transactional
  public TokenResponse register(RegisterRequest request) {
    otpService.validateAndConsume(request.phoneNumber(), request.otp());
    if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
      throw new BusinessException(HttpStatus.CONFLICT, "USER_EXISTS", "Compte déjà existant");
    }
    boolean wantsDriver = Boolean.TRUE.equals(request.registerAsDriver());
    if (wantsDriver && !authProperties.allowDriverSelfRegistration()) {
      throw new BusinessException(
          HttpStatus.FORBIDDEN,
          "DRIVER_REGISTRATION_DISABLED",
          "L’inscription conducteur n’est pas activée sur ce serveur.");
    }
    Role role = wantsDriver ? Role.DRIVER : Role.USER;
    User user =
        new User(request.phoneNumber(), passwordEncoder.encode(request.password()), role);
    userRepository.save(user);
    return issueTokens(user);
  }

  public TokenResponse login(LoginRequest request) {
    User user =
        userRepository
            .findByPhoneNumber(request.phoneNumber())
            .orElseThrow(
                () ->
                    new BusinessException(
                        HttpStatus.UNAUTHORIZED, "BAD_CREDENTIALS", "Identifiants invalides"));
    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      throw new BusinessException(
          HttpStatus.UNAUTHORIZED, "BAD_CREDENTIALS", "Identifiants invalides");
    }
    return issueTokens(user);
  }

  public TokenResponse refresh(RefreshRequest request) {
    Claims claims;
    try {
      claims = jwtService.parseAndValidate(request.refreshToken(), JwtService.TYP_REFRESH);
    } catch (ExpiredJwtException e) {
      throw new BusinessException(HttpStatus.UNAUTHORIZED, "REFRESH_EXPIRED", "Session expirée");
    } catch (JwtException e) {
      throw new BusinessException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH", "Jeton de session invalide");
    }
    long userId = Long.parseLong(claims.getSubject());
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () ->
                    new BusinessException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", "Utilisateur introuvable"));
    return issueTokens(user);
  }

  private TokenResponse issueTokens(User user) {
    String access =
        jwtService.createAccessToken(user.getId(), user.getPhoneNumber(), user.getRole());
    String refresh =
        jwtService.createRefreshToken(user.getId(), user.getPhoneNumber(), user.getRole());
    long expiresSec = jwtProperties.accessExpirationMs() / 1000;
    return new TokenResponse(access, refresh, "Bearer", expiresSec);
  }
}
