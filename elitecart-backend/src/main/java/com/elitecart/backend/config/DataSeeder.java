package com.elitecart.backend.config;

import com.elitecart.backend.entity.Cart;
import com.elitecart.backend.entity.Role;
import com.elitecart.backend.entity.RoleName;
import com.elitecart.backend.entity.User;
import com.elitecart.backend.entity.Wishlist;
import com.elitecart.backend.repository.CartRepository;
import com.elitecart.backend.repository.RoleRepository;
import com.elitecart.backend.repository.UserRepository;
import com.elitecart.backend.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * Seeds the roles table with ROLE_ADMIN / ROLE_CUSTOMER and creates a default
 * admin account on first startup, so the app is immediately usable after
 * `docker-compose up` without any manual SQL.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final WishlistRepository wishlistRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@elitecart.com}")
    private String adminEmail;

    @Value("${app.admin.password:Admin@123}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_ADMIN).build()));
        Role customerRole = roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_CUSTOMER).build()));

        if (!userRepository.existsByEmail(adminEmail)) {
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);

            User admin = User.builder()
                    .firstName("Admin")
                    .lastName("User")
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .enabled(true)
                    .roles(roles)
                    .build();
            User savedAdmin = userRepository.save(admin);

            cartRepository.save(Cart.builder().user(savedAdmin).build());
            wishlistRepository.save(Wishlist.builder().user(savedAdmin).build());

            log.info("Seeded default admin account -> email: {} / password: {}", adminEmail, adminPassword);
        }
    }
}
