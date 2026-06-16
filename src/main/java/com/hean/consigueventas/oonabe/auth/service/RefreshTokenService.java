package com.hean.consigueventas.oonabe.auth.service;

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
public class RefreshTokenService {

    @Value("${api.security.refresh.expiration}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByTokenHash(hashToken(token))
                .map(refreshToken -> {
                    refreshToken.setToken(token);
                    return refreshToken;
                });
    }

    @Transactional
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
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException("Refresh token has expired. Please make a new signin request");
        }
        return token;
    }

    @Transactional
    public int deleteByUser(Long userId) {
        return userRepository.findById(userId)
                .map(refreshTokenRepository::deleteByUser)
                .orElse(0);
    }

    @Transactional
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
