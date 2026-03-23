package com.garemobilegb.auth.controller;

import com.garemobilegb.auth.dto.UpdateUserRoleRequest;
import com.garemobilegb.auth.dto.UserRoleResponse;
import com.garemobilegb.auth.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

  private final AdminUserService adminUserService;

  public AdminUserController(AdminUserService adminUserService) {
    this.adminUserService = adminUserService;
  }

  /** Change le rôle d’un utilisateur (promotion conducteur, rétrogradation, etc.). */
  @PatchMapping("/{id}/role")
  public UserRoleResponse updateRole(
      @PathVariable long id, @Valid @RequestBody UpdateUserRoleRequest body) {
    return adminUserService.updateRole(id, body.role());
  }
}
