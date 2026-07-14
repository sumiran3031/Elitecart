package com.elitecart.backend.service;

import com.elitecart.backend.dto.auth.AuthResponse;
import com.elitecart.backend.dto.auth.ChangePasswordRequest;
import com.elitecart.backend.dto.auth.ForgotPasswordRequest;
import com.elitecart.backend.dto.auth.LoginRequest;
import com.elitecart.backend.dto.auth.RegisterRequest;
import com.elitecart.backend.dto.auth.ResetPasswordRequest;

public interface AuthService {

    void register(RegisterRequest request);

    void verifyEmail(String token);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(String refreshToken);

    void logout(Long userId);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);
}
