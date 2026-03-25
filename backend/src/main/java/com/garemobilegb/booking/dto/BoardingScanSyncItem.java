package com.garemobilegb.booking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

/** Une ligne de log de scan côté client (SQLite) rejouée vers l’API (Phase 2.3). */
public record BoardingScanSyncItem(
    @NotNull Long clientLogId,
    @NotBlank String qrToken,
    @JsonInclude(JsonInclude.Include.NON_NULL) Instant scannedAt) {}
