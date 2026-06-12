package com.hean.consigueventas.oonabe.auth.dto;

import java.util.Set;

public record JwtResponse(String token, String refreshToken, String type, Long id, String username, String email, Set<String> roles) {
    public JwtResponse(String token, String refreshToken, Long id, String username, String email, Set<String> roles) {
        this(token, refreshToken, "Bearer", id, username, email, roles);
    }
}
