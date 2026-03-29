package com.garemobilegb.auth.controller;

import com.garemobilegb.admin.dto.AdminUserSummaryResponse;
import com.garemobilegb.auth.dto.UpdateUserRoleRequest;
import com.garemobilegb.auth.dto.UserRoleResponse;
import com.garemobilegb.auth.service.AdminUserService;
import com.garemobilegb.shared.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

  private final AdminUserService adminUserService;

  public AdminUserController(AdminUserService adminUserService) {
    this.adminUserService = adminUserService;
  }

  @GetMapping
  public Page<AdminUserSummaryResponse> list(
      @RequestParam(required = false) String q, @PageableDefault(size = 20) Pageable pageable) {
    return adminUserService.list(q, pageable);
  }

  @GetMapping("/{id}")
  public AdminUserSummaryResponse getById(@PathVariable long id) {
    return adminUserService.getById(id);
  }

  /** Change le rôle d’un utilisateur (promotion conducteur, rétrogradation, etc.). */
  @PatchMapping("/{id}/role")
  public UserRoleResponse updateRole(
      @PathVariable long id,
      @Valid @RequestBody UpdateUserRoleRequest body,
      @AuthenticationPrincipal UserPrincipal principal) {
    return adminUserService.updateRole(id, body.role(), principal.getId());
  }
}
