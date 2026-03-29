package com.garemobilegb.admin.controller;

import com.garemobilegb.admin.dto.AdminStationResponse;
import com.garemobilegb.admin.dto.AdminStationWriteRequest;
import com.garemobilegb.admin.service.AdminStationService;
import com.garemobilegb.shared.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/stations")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStationController {

  private final AdminStationService adminStationService;

  public AdminStationController(AdminStationService adminStationService) {
    this.adminStationService = adminStationService;
  }

  @GetMapping
  public Page<AdminStationResponse> list(
      @RequestParam(defaultValue = "false") boolean includeArchived,
      @PageableDefault(size = 50, sort = "name") Pageable pageable) {
    return adminStationService.list(includeArchived, pageable);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public AdminStationResponse create(
      @Valid @RequestBody AdminStationWriteRequest body, @AuthenticationPrincipal UserPrincipal principal) {
    return adminStationService.create(body, principal.getId());
  }

  @PutMapping("/{stationId}")
  public AdminStationResponse update(
      @PathVariable long stationId,
      @Valid @RequestBody AdminStationWriteRequest body,
      @AuthenticationPrincipal UserPrincipal principal) {
    return adminStationService.update(stationId, body, principal.getId());
  }

  @PostMapping("/{stationId}/archive")
  public AdminStationResponse archive(
      @PathVariable long stationId, @AuthenticationPrincipal UserPrincipal principal) {
    return adminStationService.archive(stationId, principal.getId());
  }
}
