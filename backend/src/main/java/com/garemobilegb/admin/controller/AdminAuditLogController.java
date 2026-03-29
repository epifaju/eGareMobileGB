package com.garemobilegb.admin.controller;

import com.garemobilegb.admin.dto.AdminAuditEntryResponse;
import com.garemobilegb.audit.domain.AdminAuditLogEntry;
import com.garemobilegb.audit.repository.AdminAuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/audit-log")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAuditLogController {

  private final AdminAuditLogRepository adminAuditLogRepository;

  public AdminAuditLogController(AdminAuditLogRepository adminAuditLogRepository) {
    this.adminAuditLogRepository = adminAuditLogRepository;
  }

  @GetMapping
  public Page<AdminAuditEntryResponse> list(@PageableDefault(size = 50) Pageable pageable) {
    Page<AdminAuditLogEntry> page = adminAuditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    return page.map(AdminAuditEntryResponse::from);
  }
}
