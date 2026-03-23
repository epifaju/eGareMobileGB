package com.garemobilegb.auth.service;

import com.garemobilegb.auth.domain.Role;
import com.garemobilegb.auth.domain.User;
import com.garemobilegb.auth.dto.UserRoleResponse;
import com.garemobilegb.auth.repository.UserRepository;
import com.garemobilegb.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService {

  private final UserRepository userRepository;

  public AdminUserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional
  public UserRoleResponse updateRole(long userId, Role newRole) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () ->
                    new BusinessException(
                        HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Utilisateur introuvable"));
    user.setRole(newRole);
    return UserRoleResponse.from(userRepository.save(user));
  }
}
