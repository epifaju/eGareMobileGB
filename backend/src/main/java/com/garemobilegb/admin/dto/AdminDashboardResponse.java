package com.garemobilegb.admin.dto;

import java.util.Map;

public record AdminDashboardResponse(
    long totalUsers,
    Map<String, Long> usersByRole,
    long activeStations,
    long archivedStations,
    long activeVehicles,
    long archivedVehicles,
    Map<String, Long> activeVehiclesByStatus,
    long bookingsTodayUtc) {}
