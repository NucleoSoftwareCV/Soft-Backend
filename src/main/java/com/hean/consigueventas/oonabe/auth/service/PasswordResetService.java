package com.hean.consigueventas.oonabe.auth.service;

import com.hean.consigueventas.oonabe.auth.entity.PasswordResetToken;
import com.hean.consigueventas.oonabe.auth.repository.PasswordResetTokenRepository;
import com.hean.consigueventas.oonabe.common.email.EmailService;
import com.hean.consigueventas.oonabe.common.exception.BusinessLogicException;
import com.hean.consigueventas.oonabe.user.entity.User;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class PasswordResetService {

    private static final Duration TOKEN_TTL = Duration.ofHours(1);

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;
    private final String frontendUrl;

    public PasswordResetService(
            PasswordResetTokenRepository tokenRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RefreshTokenService refreshTokenService,
            EmailService emailService,
            @Value("${app.frontend-url}") String frontendUrl) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.emailService = emailService;
        this.frontendUrl = frontendUrl;
    }

    // Publico: no revela si el email existe, para evitar enumeracion de cuentas.
    @Transactional
    public void requestReset(String email) {
        userRepository.findByUsernameOrEmail(email, email).ifPresent(user -> {
            tokenRepository.deleteByUser(user);
            tokenRepository.flush();

            String rawToken = UUID.randomUUID().toString();

            PasswordResetToken token = new PasswordResetToken();
            token.setUser(user);
            token.setTokenHash(hashToken(rawToken));
            token.setExpiryDate(Instant.now().plus(TOKEN_TTL));
            tokenRepository.save(token);

            String resetLink = frontendUrl + "/auth/reset-password?token=" + rawToken;
            emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
        });
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken token = tokenRepository.findByTokenHash(hashToken(rawToken))
                .orElseThrow(() -> new BusinessLogicException("El enlace de recuperación no es válido."));

        if (token.isUsed() || token.getExpiryDate().isBefore(Instant.now())) {
            throw new BusinessLogicException("El enlace de recuperación expiró o ya fue usado. Solicita uno nuevo.");
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.setUsed(true);
        tokenRepository.save(token);

        // Cambiar la contraseña cierra todas las sesiones activas del usuario.
        refreshTokenService.deleteByUser(user.getId());
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
