package com.garemobilegb.notification.service;

import com.garemobilegb.auth.domain.User;
import com.garemobilegb.auth.repository.UserRepository;
import com.garemobilegb.notification.domain.UserPushToken;
import com.garemobilegb.notification.dto.RegisterPushTokenRequest;
import com.garemobilegb.notification.repository.UserPushTokenRepository;
import com.garemobilegb.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PushTokenService {

  private final UserPushTokenRepository userPushTokenRepository;
  private final UserRepository userRepository;

  public PushTokenService(UserPushTokenRepository userPushTokenRepository, UserRepository userRepository) {
    this.userPushTokenRepository = userPushTokenRepository;
    this.userRepository = userRepository;
  }

  @Transactional
  public void register(long userId, RegisterPushTokenRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> new BusinessException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Utilisateur introuvable"));
    String token = request.expoPushToken().trim();
    userPushTokenRepository
        .findByUser_IdAndExpoPushToken(userId, token)
        .ifPresentOrElse(
            t -> {
              t.touch();
              userPushTokenRepository.save(t);
            },
            () -> userPushTokenRepository.save(new UserPushToken(user, token)));
  }

  @Transactional
  public void unregister(long userId, RegisterPushTokenRequest request) {
    userPushTokenRepository.deleteByUser_IdAndExpoPushToken(userId, request.expoPushToken().trim());
  }
}
