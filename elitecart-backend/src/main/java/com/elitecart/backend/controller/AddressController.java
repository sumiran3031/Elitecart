package com.elitecart.backend.controller;

import com.elitecart.backend.dto.address.AddressRequest;
import com.elitecart.backend.dto.address.AddressResponse;
import com.elitecart.backend.dto.common.ApiResponse;
import com.elitecart.backend.security.UserPrincipal;
import com.elitecart.backend.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Customer's own shipping/billing addresses (used at checkout).
 */
@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Addresses", description = "Manage the current customer's shipping & billing addresses")
public class AddressController {

    private final AddressService addressService;

    @Operation(summary = "List all addresses for the current user")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAll(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(addressService.getAllForUser(principal.getId())));
    }

    @Operation(summary = "Get a single address by id")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> getById(@AuthenticationPrincipal UserPrincipal principal,
                                                                 @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(addressService.getById(principal.getId(), id)));
    }

    @Operation(summary = "Add a new address")
    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> create(@AuthenticationPrincipal UserPrincipal principal,
                                                                @Valid @RequestBody AddressRequest request) {
        AddressResponse response = addressService.create(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Address added successfully", response));
    }

    @Operation(summary = "Update an existing address")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> update(@AuthenticationPrincipal UserPrincipal principal,
                                                                @PathVariable Long id,
                                                                @Valid @RequestBody AddressRequest request) {
        AddressResponse response = addressService.update(principal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Address updated successfully", response));
    }

    @Operation(summary = "Delete an address")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal UserPrincipal principal,
                                                     @PathVariable Long id) {
        addressService.delete(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.message("Address deleted successfully"));
    }
}
