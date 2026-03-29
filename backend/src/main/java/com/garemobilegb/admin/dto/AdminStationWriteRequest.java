package com.garemobilegb.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminStationWriteRequest(
    @NotBlank @Size(max = 160) String name,
    @Size(max = 120) String city,
    @NotNull Double latitude,
    @NotNull Double longitude,
    @Size(max = 500) String description) {}
