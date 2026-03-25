package com.garemobilegb.notification.controller;

import com.garemobilegb.notification.dto.RegisterPushTokenRequest;
import com.garemobilegb.notification.service.PushTokenService;
import com.garemobilegb.shared.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me/push-token")
public class PushTokenController {

  private final PushTokenService pushTokenService;

  public PushTokenController(PushTokenService pushTokenService) {
    this.pushTokenService = pushTokenService;
  }

  /** Enregistre un jeton Expo Push pour les alertes conducteur (remplissage). */
  @PostMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void register(
      @AuthenticationPrincipal UserPrincipal principal,
      @Valid @RequestBody RegisterPushTokenRequest body) {
    pushTokenService.register(principal.getId(), body);
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void unregister(
      @AuthenticationPrincipal UserPrincipal principal,
      @Valid @RequestBody RegisterPushTokenRequest body) {
    pushTokenService.unregister(principal.getId(), body);
  }
}
