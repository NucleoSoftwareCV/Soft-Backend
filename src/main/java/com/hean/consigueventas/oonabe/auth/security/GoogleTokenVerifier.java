package com.hean.consigueventas.oonabe.auth.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class GoogleTokenVerifier {

    private final RestTemplate restTemplate;
    private final String expectedClientId;

    public GoogleTokenVerifier(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${app.security.google.client-id:}") String expectedClientId) {
        this.restTemplate = restTemplateBuilder.build();
        this.expectedClientId = expectedClientId;
    }

    public Map<String, Object> verifyToken(String idToken) {
        String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
        Map<String, Object> response;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = restTemplate.getForObject(url, Map.class);
            response = parsed;
        } catch (Exception e) {
            throw new BadCredentialsException("Error al validar el token de Google: " + e.getMessage());
        }

        if (response == null || !"true".equals(String.valueOf(response.get("email_verified")))) {
            throw new BadCredentialsException("Token de Google inválido o correo no verificado.");
        }

        // El claim "aud" debe coincidir con nuestro Client ID: sin esta comprobación,
        // un id_token valido emitido para CUALQUIER otra app de Google pasaria la verificacion.
        if (!StringUtils.hasText(expectedClientId)) {
            throw new IllegalStateException(
                    "app.security.google.client-id no esta configurado; no se puede validar el login con Google.");
        }
        if (!expectedClientId.equals(response.get("aud"))) {
            throw new BadCredentialsException("El token de Google no fue emitido para esta aplicacion.");
        }

        return response;
    }
}
