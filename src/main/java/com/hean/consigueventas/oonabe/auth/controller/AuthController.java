package com.hean.consigueventas.oonabe.auth.controller;

import com.hean.consigueventas.oonabe.auth.dto.JwtResponse;
import com.hean.consigueventas.oonabe.auth.dto.LoginRequest;
import com.hean.consigueventas.oonabe.auth.dto.RegisterRequest;
import com.hean.consigueventas.oonabe.auth.dto.TokenRefreshRequest;
import com.hean.consigueventas.oonabe.auth.dto.TokenRefreshResponse;
import com.hean.consigueventas.oonabe.auth.service.AuthService;
import com.hean.consigueventas.oonabe.user.dto.UserDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public JwtResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh-token")
    public TokenRefreshResponse refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        return authService.refreshToken(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody TokenRefreshRequest request) {
        authService.logout(request);
    }
}
