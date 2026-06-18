package com.hean.consigueventas.oonabe.auth.service;

import com.hean.consigueventas.oonabe.auth.entity.RefreshToken;

import java.util.Optional;

public interface IRefreshTokenService {
    Optional<RefreshToken> findByToken(String token);
    RefreshToken createRefreshToken(Long userId);
    RefreshToken verifyExpiration(RefreshToken token);
    int deleteByUser(Long userId);
    void deleteByToken(String token);
}
