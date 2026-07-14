package com.elitecart.backend.service.impl;

import com.elitecart.backend.dto.auth.UserResponse;
import com.elitecart.backend.entity.RoleName;
import com.elitecart.backend.mapper.UserMapper;
import com.elitecart.backend.repository.UserRepository;
import com.elitecart.backend.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getCustomers(Pageable pageable) {
        return userRepository.findByRolesName(RoleName.ROLE_CUSTOMER, pageable).map(userMapper::toUserResponse);
    }
}
