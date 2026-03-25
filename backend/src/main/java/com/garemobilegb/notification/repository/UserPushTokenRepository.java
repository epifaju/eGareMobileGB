package com.garemobilegb.notification.repository;

import com.garemobilegb.auth.domain.Role;
import com.garemobilegb.notification.domain.UserPushToken;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPushTokenRepository extends JpaRepository<UserPushToken, Long> {

  Optional<UserPushToken> findByUser_IdAndExpoPushToken(long userId, String expoPushToken);

  List<UserPushToken> findByUser_Role(Role role);

  void deleteByUser_IdAndExpoPushToken(long userId, String expoPushToken);
}
