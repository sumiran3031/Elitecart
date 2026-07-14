package com.elitecart.backend.controller;

import com.elitecart.backend.dto.auth.UserResponse;
import com.elitecart.backend.dto.common.ApiResponse;
import com.elitecart.backend.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin-only customer listing. Routed under /admin/** which SecurityConfig
 * restricts to ROLE_ADMIN.
 */
@RestController
@RequestMapping("/admin/customers")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin - Customers", description = "Customer listing APIs (ADMIN only)")
public class AdminCustomerController {

    private final AdminUserService adminUserService;

    @Operation(summary = "List all customers (paginated)")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getCustomers(
            @Parameter(hidden = true) @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(adminUserService.getCustomers(pageable)));
    }
}
