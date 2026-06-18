package com.hean.consigueventas.oonabe.auth.service.impl;

import com.hean.consigueventas.oonabe.auth.service.IRefreshTokenService;

import com.hean.consigueventas.oonabe.auth.entity.RefreshToken;
import com.hean.consigueventas.oonabe.auth.repository.RefreshTokenRepository;
import com.hean.consigueventas.oonabe.common.exception.TokenRefreshException;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements IRefreshTokenService {

    @Value("${api.security.refresh.expiration}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByTokenHash(hashToken(token))
                .map(refreshToken -> {
                    refreshToken.setToken(token);
                    return refreshToken;
                });
    }

    @Transactional
    @Override
    public RefreshToken createRefreshToken(Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush();

        String rawToken = UUID.randomUUID().toString();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setTokenHash(hashToken(rawToken));

        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        saved.setToken(rawToken);
        return saved;
    }

    @Transactional
    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException("El refresh token ha expirado. Inicia sesión nuevamente.");
        }
        return token;
    }

    @Transactional
    @Override
    public int deleteByUser(Long userId) {
        return userRepository.findById(userId)
                .map(refreshTokenRepository::deleteByUser)
                .orElse(0);
    }

    @Transactional
    @Override
    public void deleteByToken(String token) {
        refreshTokenRepository.deleteByTokenHash(hashToken(token));
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
