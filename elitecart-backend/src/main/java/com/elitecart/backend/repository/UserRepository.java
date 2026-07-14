package com.elitecart.backend.repository;

import com.elitecart.backend.entity.RoleName;
import com.elitecart.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByEmailVerificationToken(String token);
    Optional<User> findByResetPasswordToken(String token);
    Optional<User> findByRefreshToken(String refreshToken);
    long countByRolesName(RoleName roleName);
    Page<User> findByRolesName(RoleName roleName, Pageable pageable);
}
