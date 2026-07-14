package com.elitecart.backend.repository;

import com.elitecart.backend.entity.Role;
import com.elitecart.backend.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
