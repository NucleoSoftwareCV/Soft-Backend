package com.hean.consigueventas.oonabe.auth;

import com.hean.consigueventas.oonabe.auth.security.GoogleTokenVerifier;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GoogleTokenVerifierTest {

    private static final String TOKEN_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=some-token";
    private static final String OUR_CLIENT_ID = "our-client-id.apps.googleusercontent.com";

    @Test
    void acceptsATokenIssuedForOurClientId() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo(TOKEN_URL)).andRespond(withSuccess("""
                {"email":"user@example.com","email_verified":"true","aud":"%s"}
                """.formatted(OUR_CLIENT_ID), MediaType.APPLICATION_JSON));

        GoogleTokenVerifier verifier = new GoogleTokenVerifier(fixedBuilder(restTemplate), OUR_CLIENT_ID);

        var result = verifier.verifyToken("some-token");

        assertThat(result.get("email")).isEqualTo("user@example.com");
    }

    @Test
    void rejectsATokenIssuedForADifferentGoogleClient() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo(TOKEN_URL)).andRespond(withSuccess("""
                {"email":"user@example.com","email_verified":"true","aud":"someone-elses-app.apps.googleusercontent.com"}
                """, MediaType.APPLICATION_JSON));

        GoogleTokenVerifier verifier = new GoogleTokenVerifier(fixedBuilder(restTemplate), OUR_CLIENT_ID);

        assertThatThrownBy(() -> verifier.verifyToken("some-token"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void rejectsAnUnverifiedEmail() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo(TOKEN_URL)).andRespond(withSuccess("""
                {"email":"user@example.com","email_verified":"false","aud":"%s"}
                """.formatted(OUR_CLIENT_ID), MediaType.APPLICATION_JSON));

        GoogleTokenVerifier verifier = new GoogleTokenVerifier(fixedBuilder(restTemplate), OUR_CLIENT_ID);

        assertThatThrownBy(() -> verifier.verifyToken("some-token"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void failsFastWhenNoClientIdIsConfigured() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo(TOKEN_URL)).andRespond(withSuccess("""
                {"email":"user@example.com","email_verified":"true","aud":"%s"}
                """.formatted(OUR_CLIENT_ID), MediaType.APPLICATION_JSON));

        GoogleTokenVerifier verifier = new GoogleTokenVerifier(fixedBuilder(restTemplate), "");

        assertThatThrownBy(() -> verifier.verifyToken("some-token"))
                .isInstanceOf(IllegalStateException.class);
    }

    private RestTemplateBuilder fixedBuilder(RestTemplate restTemplate) {
        return new RestTemplateBuilder() {
            @Override
            public RestTemplate build() {
                return restTemplate;
            }
        };
    }
}
