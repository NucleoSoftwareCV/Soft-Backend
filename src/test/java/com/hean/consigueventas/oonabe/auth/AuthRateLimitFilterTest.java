package com.hean.consigueventas.oonabe.auth;

import com.hean.consigueventas.oonabe.auth.security.AuthRateLimitFilter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class AuthRateLimitFilterTest {

    @ParameterizedTest
    @ValueSource(strings = {"/api/auth/login", "/api/v1/auth/admin/login"})
    void limitsPublicAndAdministrativeLoginEndpoints(String path) throws Exception {
        AuthRateLimitFilter filter = new AuthRateLimitFilter(2, 60_000);

        assertThat(execute(filter, path).getStatus()).isEqualTo(200);
        assertThat(execute(filter, path).getStatus()).isEqualTo(200);

        MockHttpServletResponse limited = execute(filter, path);
        assertThat(limited.getStatus()).isEqualTo(429);
        assertThat(limited.getContentType()).isEqualTo("application/problem+json");
    }

    private MockHttpServletResponse execute(AuthRateLimitFilter filter, String path)
            throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }
}
