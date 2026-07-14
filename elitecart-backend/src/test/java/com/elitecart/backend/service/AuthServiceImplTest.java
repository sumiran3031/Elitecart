package com.elitecart.backend.service;

import com.elitecart.backend.dto.auth.RegisterRequest;
import com.elitecart.backend.entity.Cart;
import com.elitecart.backend.entity.Role;
import com.elitecart.backend.entity.RoleName;
import com.elitecart.backend.entity.User;
import com.elitecart.backend.entity.Wishlist;
import com.elitecart.backend.exception.DuplicateResourceException;
import com.elitecart.backend.mapper.UserMapper;
import com.elitecart.backend.repository.CartRepository;
import com.elitecart.backend.repository.RoleRepository;
import com.elitecart.backend.repository.UserRepository;
import com.elitecart.backend.repository.WishlistRepository;
import com.elitecart.backend.security.JwtUtil;
import com.elitecart.backend.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AuthServiceImpl covering the registration flow,
 * including the duplicate-email guard rail.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private WishlistRepository wishlistRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private EmailService emailService;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "frontendBaseUrl", "http://localhost:5173");
        registerRequest = RegisterRequest.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .password("Password123")
                .build();
    }

    @Test
    void register_shouldThrowDuplicateResourceException_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(registerRequest));

        verify(userRepository, org.mockito.Mockito.never()).save(any(User.class));
    }

    @Test
    void register_shouldCreateUserCartAndWishlist_whenEmailIsNew() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByName(RoleName.ROLE_CUSTOMER))
                .thenReturn(Optional.of(Role.builder().name(RoleName.ROLE_CUSTOMER).build()));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");

        User savedUser = User.builder().email(registerRequest.getEmail()).build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        authService.register(registerRequest);

        verify(userRepository).save(any(User.class));
        verify(cartRepository).save(any(Cart.class));
        verify(wishlistRepository).save(any(Wishlist.class));
        verify(emailService).sendVerificationEmail(any(User.class), anyString());
    }
}
