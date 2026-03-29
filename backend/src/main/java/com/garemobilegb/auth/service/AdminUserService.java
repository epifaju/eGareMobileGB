package com.garemobilegb.auth.service;

import com.garemobilegb.admin.dto.AdminUserSummaryResponse;
import com.garemobilegb.audit.service.AdminAuditLogService;
import com.garemobilegb.auth.domain.Role;
import com.garemobilegb.auth.domain.User;
import com.garemobilegb.auth.dto.UserRoleResponse;
import com.garemobilegb.auth.repository.UserRepository;
import com.garemobilegb.shared.exceptions.BusinessException;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService {

  private final UserRepository userRepository;
  private final AdminAuditLogService adminAuditLogService;

  public AdminUserService(UserRepository userRepository, AdminAuditLogService adminAuditLogService) {
    this.userRepository = userRepository;
    this.adminAuditLogService = adminAuditLogService;
  }

  @Transactional(readOnly = true)
  public Page<AdminUserSummaryResponse> list(String phoneQuery, Pageable pageable) {
    if (phoneQuery != null && !phoneQuery.isBlank()) {
      return userRepository
          .findByPhoneNumberContainingIgnoreCase(phoneQuery.trim(), pageable)
          .map(AdminUserSummaryResponse::from);
    }
    return userRepository.findAll(pageable).map(AdminUserSummaryResponse::from);
  }

  @Transactional(readOnly = true)
  public AdminUserSummaryResponse getById(long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () ->
                    new BusinessException(
                        HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Utilisateur introuvable"));
    return AdminUserSummaryResponse.from(user);
  }

  @Transactional
  public UserRoleResponse updateRole(long userId, Role newRole, long actorUserId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () ->
                    new BusinessException(
                        HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Utilisateur introuvable"));
    Role previous = user.getRole();
    user.setRole(newRole);
    User saved = userRepository.save(user);
    adminAuditLogService.record(
        actorUserId,
        "USER_ROLE_UPDATED",
        "User",
        saved.getId(),
        Map.of("previousRole", previous.name(), "newRole", newRole.name()));
    return UserRoleResponse.from(saved);
  }
}
