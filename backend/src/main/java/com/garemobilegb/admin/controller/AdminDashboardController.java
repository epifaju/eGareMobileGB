package com.garemobilegb.admin.controller;

import com.garemobilegb.admin.dto.AdminDashboardResponse;
import com.garemobilegb.admin.service.AdminDashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

  private final AdminDashboardService adminDashboardService;

  public AdminDashboardController(AdminDashboardService adminDashboardService) {
    this.adminDashboardService = adminDashboardService;
  }

  @GetMapping
  public AdminDashboardResponse getDashboard() {
    return adminDashboardService.snapshot();
  }
}
