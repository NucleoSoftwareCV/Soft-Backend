package com.hean.consigueventas.oonabe.auth.dto;

import java.util.Set;

public record JwtResponse(String token, String type, Long id, String username, String email, Set<String> roles) {
    public JwtResponse(String token, Long id, String username, String email, Set<String> roles) {
        this(token, "Bearer", id, username, email, roles);
    }
}
