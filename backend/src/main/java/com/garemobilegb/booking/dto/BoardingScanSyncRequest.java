package com.garemobilegb.booking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record BoardingScanSyncRequest(@NotEmpty @Valid List<BoardingScanSyncItem> scans) {}
