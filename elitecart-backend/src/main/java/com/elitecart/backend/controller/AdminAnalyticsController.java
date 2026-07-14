package com.elitecart.backend.controller;

import com.elitecart.backend.dto.analytics.DashboardStatsResponse;
import com.elitecart.backend.dto.common.ApiResponse;
import com.elitecart.backend.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin-only analytics/dashboard endpoints. Routed under /admin/** which
 * SecurityConfig restricts to ROLE_ADMIN.
 */
@RestController
@RequestMapping("/admin/analytics")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin - Analytics", description = "Dashboard statistics, charts, and CSV export (ADMIN only)")
public class AdminAnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "Get dashboard stats: revenue, orders, products, customers, latest orders, " +
            "top products, 12-month revenue trend, and revenue by category")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getDashboardStats()));
    }

    @Operation(summary = "Export every order as a downloadable CSV file")
    @GetMapping("/orders/export")
    public ResponseEntity<String> exportOrdersCsv() {
        String csv = analyticsService.exportOrdersCsv();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("elitecart-orders.csv").build().toString())
                .body(csv);
    }
}
