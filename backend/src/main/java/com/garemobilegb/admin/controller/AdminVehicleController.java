package com.garemobilegb.admin.controller;

import com.garemobilegb.admin.dto.AdminVehicleResponse;
import com.garemobilegb.admin.dto.CreateAdminVehicleRequest;
import com.garemobilegb.admin.dto.UpdateAdminVehicleRequest;
import com.garemobilegb.admin.service.AdminVehicleService;
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
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminVehicleController {

  private final AdminVehicleService adminVehicleService;

  public AdminVehicleController(AdminVehicleService adminVehicleService) {
    this.adminVehicleService = adminVehicleService;
  }

  @GetMapping("/vehicles")
  public Page<AdminVehicleResponse> list(
      @RequestParam(required = false) Long stationId,
      @RequestParam(defaultValue = "false") boolean includeArchived,
      @PageableDefault(size = 50, sort = "id") Pageable pageable) {
    return adminVehicleService.list(stationId, includeArchived, pageable);
  }

  @PostMapping("/stations/{stationId}/vehicles")
  @ResponseStatus(HttpStatus.CREATED)
  public AdminVehicleResponse create(
      @PathVariable long stationId,
      @Valid @RequestBody CreateAdminVehicleRequest body,
      @AuthenticationPrincipal UserPrincipal principal) {
    return adminVehicleService.create(stationId, body, principal.getId());
  }

  @PutMapping("/vehicles/{vehicleId}")
  public AdminVehicleResponse update(
      @PathVariable long vehicleId,
      @Valid @RequestBody UpdateAdminVehicleRequest body,
      @AuthenticationPrincipal UserPrincipal principal) {
    return adminVehicleService.update(vehicleId, body, principal.getId());
  }

  @PostMapping("/vehicles/{vehicleId}/archive")
  public AdminVehicleResponse archive(
      @PathVariable long vehicleId, @AuthenticationPrincipal UserPrincipal principal) {
    return adminVehicleService.archive(vehicleId, principal.getId());
  }
}
