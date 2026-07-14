package com.elitecart.backend.service;

import com.elitecart.backend.dto.analytics.DashboardStatsResponse;

public interface AnalyticsService {
    DashboardStatsResponse getDashboardStats();

    /** Returns a CSV export of every order (order number, date, customer, status, total). */
    String exportOrdersCsv();
}
