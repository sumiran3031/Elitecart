package com.elitecart.backend.service;

import com.elitecart.backend.dto.auth.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {
    Page<UserResponse> getCustomers(Pageable pageable);
}
