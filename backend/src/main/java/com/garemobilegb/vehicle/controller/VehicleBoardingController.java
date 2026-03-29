package com.garemobilegb.vehicle.controller;

import com.garemobilegb.booking.dto.BoardingScanSyncRequest;
import com.garemobilegb.booking.dto.BoardingScanSyncResponse;
import com.garemobilegb.booking.dto.BoardingScanSyncResultItem;
import com.garemobilegb.booking.dto.BoardingValidationResponse;
import com.garemobilegb.booking.dto.ValidateBoardingQrRequest;
import com.garemobilegb.booking.service.BoardingValidationService;
import com.garemobilegb.shared.exceptions.BusinessException;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleBoardingController {

  private final BoardingValidationService boardingValidationService;

  public VehicleBoardingController(BoardingValidationService boardingValidationService) {
    this.boardingValidationService = boardingValidationService;
  }

  /** Scan QR passager : valide l’embarquement pour le véhicule courant (conducteur / admin). */
  @PostMapping("/{vehicleId}/boarding/validate-qr")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasAnyRole('AGENT','DRIVER','ADMIN')")
  public BoardingValidationResponse validateQr(
      @PathVariable long vehicleId, @Valid @RequestBody ValidateBoardingQrRequest body) {
    return boardingValidationService.validateForVehicle(vehicleId, body.qrToken());
  }

  /**
   * Synchronise les scans enregistrés hors ligne (table SQLite {@code boarding_scan_logs} côté
   * mobile). Même logique métier que {@link #validateQr} par élément, ordre conservé.
   */
  @PostMapping("/{vehicleId}/boarding/sync-scans")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasAnyRole('AGENT','DRIVER','ADMIN')")
  public BoardingScanSyncResponse syncScans(
      @PathVariable long vehicleId, @Valid @RequestBody BoardingScanSyncRequest body) {
    List<BoardingScanSyncResultItem> results = new ArrayList<>();
    for (var item : body.scans()) {
      long logId = item.clientLogId();
      try {
        boardingValidationService.validateForVehicle(vehicleId, item.qrToken());
        results.add(BoardingScanSyncResultItem.synced(logId));
      } catch (BusinessException e) {
        results.add(BoardingScanSyncResultItem.error(logId, e.getCode(), e.getMessage()));
      } catch (RuntimeException e) {
        results.add(
            BoardingScanSyncResultItem.error(
                logId, "UNEXPECTED", e.getMessage() != null ? e.getMessage() : "Erreur inattendue"));
      }
    }
    return new BoardingScanSyncResponse(results);
  }
}
