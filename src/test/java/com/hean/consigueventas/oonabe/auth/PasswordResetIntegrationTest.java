package com.hean.consigueventas.oonabe.auth;

import com.hean.consigueventas.oonabe.auth.entity.PasswordResetToken;
import com.hean.consigueventas.oonabe.auth.repository.PasswordResetTokenRepository;
import com.hean.consigueventas.oonabe.user.entity.User;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PasswordResetIntegrationTest {

    private static final String FORGOT_ENDPOINT = "/api/auth/forgot-password";
    private static final String RESET_ENDPOINT = "/api/auth/reset-password";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void forgotPasswordRespondsTheSameForExistingAndUnknownEmails() throws Exception {
        mockMvc.perform(post(FORGOT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "user1@oona.es"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(
                        "Si el email existe, te enviamos un enlace para restablecer tu contraseña."));

        mockMvc.perform(post(FORGOT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "no-existe-nadie-con-este-email@example.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(
                        "Si el email existe, te enviamos un enlace para restablecer tu contraseña."));
    }

    @Test
    void forgotPasswordCreatesAUsableTokenForAnExistingUser() throws Exception {
        User user = userRepository.findByUsername("user1").orElseThrow();

        mockMvc.perform(post(FORGOT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "user1@oona.es"}
                                """))
                .andExpect(status().isOk());

        assertThat(tokenRepository.count()).isEqualTo(1);
        PasswordResetToken saved = tokenRepository.findAll().get(0);
        assertThat(saved.getUser().getId()).isEqualTo(user.getId());
        assertThat(saved.isUsed()).isFalse();
        assertThat(saved.getExpiryDate()).isAfter(Instant.now());
    }

    @Test
    void resetPasswordWithAValidTokenChangesThePasswordAndConsumesTheToken() throws Exception {
        User user = userRepository.findByUsername("user2").orElseThrow();
        String rawToken = seedResetToken(user, Instant.now().plusSeconds(3600));

        mockMvc.perform(post(RESET_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token": "%s", "newPassword": "BrandNewPass123"}
                                """.formatted(rawToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        User updated = userRepository.findByUsername("user2").orElseThrow();
        assertThat(passwordEncoder.matches("BrandNewPass123", updated.getPassword())).isTrue();

        // El mismo token no puede reutilizarse.
        mockMvc.perform(post(RESET_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token": "%s", "newPassword": "OtraPass456"}
                                """.formatted(rawToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://api.oona.local/errors/business-rule"));
    }

    @Test
    void resetPasswordWithAnExpiredTokenIsRejected() throws Exception {
        User user = userRepository.findByUsername("user1").orElseThrow();
        String rawToken = seedResetToken(user, Instant.now().minusSeconds(10));

        mockMvc.perform(post(RESET_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token": "%s", "newPassword": "BrandNewPass123"}
                                """.formatted(rawToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://api.oona.local/errors/business-rule"));
    }

    @Test
    void resetPasswordWithAnUnknownTokenIsRejected() throws Exception {
        mockMvc.perform(post(RESET_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token": "does-not-exist", "newPassword": "BrandNewPass123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://api.oona.local/errors/business-rule"));
    }

    private String seedResetToken(User user, Instant expiryDate) {
        String rawToken = UUID.randomUUID().toString();
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setTokenHash(hash(rawToken));
        token.setExpiryDate(expiryDate);
        tokenRepository.saveAndFlush(token);
        return rawToken;
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
