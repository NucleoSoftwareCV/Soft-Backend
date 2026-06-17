package com.hean.consigueventas.oonabe.auth.security;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class GoogleTokenVerifier {

    private final RestTemplate restTemplate;

    public GoogleTokenVerifier() {
        this.restTemplate = new RestTemplate();
    }

    public Map<String, Object> verifyToken(String idToken) {
        String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null || !"true".equals(String.valueOf(response.get("email_verified")))) {
                throw new BadCredentialsException("Token de Google invalido o correo no verificado.");
            }
            return response;
        } catch (Exception e) {
            throw new BadCredentialsException("Error al validar el token de Google: " + e.getMessage());
        }
    }
}
