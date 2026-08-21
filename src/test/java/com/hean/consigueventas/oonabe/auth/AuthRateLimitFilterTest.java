package com.hean.consigueventas.oonabe.auth;

import com.hean.consigueventas.oonabe.auth.security.AuthRateLimitFilter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuthRateLimitFilterTest {

    @ParameterizedTest
    @ValueSource(strings = {"/api/auth/login", "/api/v1/auth/admin/login"})
    void limitsPublicAndAdministrativeLoginEndpoints(String path) throws Exception {
        AuthRateLimitFilter filter = new AuthRateLimitFilter(2, 60_000, List.of());

        assertThat(execute(filter, path).getStatus()).isEqualTo(200);
        assertThat(execute(filter, path).getStatus()).isEqualTo(200);

        MockHttpServletResponse limited = execute(filter, path);
        assertThat(limited.getStatus()).isEqualTo(429);
        assertThat(limited.getContentType()).isEqualTo("application/problem+json");
    }

    @org.junit.jupiter.api.Test
    void ignoresForwardedForHeaderFromUntrustedClients() throws Exception {
        AuthRateLimitFilter filter = new AuthRateLimitFilter(2, 60_000, List.of());

        // Un cliente no confiable intenta esquivar el limite variando X-Forwarded-For
        // en cada intento; como no esta en la lista de proxies confiables, se ignora
        // y se sigue contando por la IP real de la conexion.
        assertThat(execute(filter, "/api/auth/login", "1.1.1.1").getStatus()).isEqualTo(200);
        assertThat(execute(filter, "/api/auth/login", "2.2.2.2").getStatus()).isEqualTo(200);
        assertThat(execute(filter, "/api/auth/login", "3.3.3.3").getStatus()).isEqualTo(429);
    }

    @org.junit.jupiter.api.Test
    void honorsForwardedForHeaderFromTrustedProxies() throws Exception {
        AuthRateLimitFilter filter = new AuthRateLimitFilter(2, 60_000, List.of("127.0.0.1"));

        assertThat(execute(filter, "/api/auth/login", "1.1.1.1").getStatus()).isEqualTo(200);
        assertThat(execute(filter, "/api/auth/login", "1.1.1.1").getStatus()).isEqualTo(200);
        assertThat(execute(filter, "/api/auth/login", "1.1.1.1").getStatus()).isEqualTo(429);
    }

    private MockHttpServletResponse execute(AuthRateLimitFilter filter, String path)
            throws Exception {
        return execute(filter, path, null);
    }

    private MockHttpServletResponse execute(AuthRateLimitFilter filter, String path, String forwardedFor)
            throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
        request.setRemoteAddr("127.0.0.1");
        if (forwardedFor != null) {
            request.addHeader("X-Forwarded-For", forwardedFor);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }
}
