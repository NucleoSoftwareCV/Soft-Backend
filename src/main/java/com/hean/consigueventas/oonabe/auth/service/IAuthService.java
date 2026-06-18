package com.hean.consigueventas.oonabe.auth.service;

import com.hean.consigueventas.oonabe.auth.dto.request.GoogleLoginRequest;
import com.hean.consigueventas.oonabe.auth.dto.request.LoginRequest;
import com.hean.consigueventas.oonabe.auth.dto.request.RegisterRequest;
import com.hean.consigueventas.oonabe.auth.dto.request.TokenRefreshRequest;
import com.hean.consigueventas.oonabe.auth.dto.response.JwtResponse;
import com.hean.consigueventas.oonabe.auth.dto.response.TokenRefreshResponse;
import com.hean.consigueventas.oonabe.user.dto.response.UserResponse;

public interface IAuthService {
    UserResponse register(RegisterRequest request);
    JwtResponse login(LoginRequest request);
    JwtResponse loginWithGoogle(GoogleLoginRequest request);
    TokenRefreshResponse refreshToken(TokenRefreshRequest request);
    void logout(TokenRefreshRequest request);
}
