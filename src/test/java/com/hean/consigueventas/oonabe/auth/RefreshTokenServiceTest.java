package com.hean.consigueventas.oonabe.auth;

import com.hean.consigueventas.oonabe.auth.repository.RefreshTokenRepository;
import com.hean.consigueventas.oonabe.auth.service.IRefreshTokenService;
import com.hean.consigueventas.oonabe.user.entity.User;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class RefreshTokenServiceTest {

    @Autowired
    private IRefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createRefreshTokenStoresHashInsteadOfRawToken() {
        User user = userRepository.findByUsername("user1").orElseThrow();

        String rawToken = refreshTokenService.createRefreshToken(user.getId()).getToken();
        String storedToken = refreshTokenRepository.findAll().stream()
                .filter(token -> token.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElseThrow()
                .getToken();

        assertThat(storedToken).isNotEqualTo(rawToken);
        assertThat(refreshTokenService.findByToken(rawToken)).isPresent();
    }
}
