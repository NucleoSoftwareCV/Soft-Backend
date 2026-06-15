package com.hean.consigueventas.oonabe.auth;

import com.hean.consigueventas.oonabe.auth.security.GoogleTokenVerifier;
import com.hean.consigueventas.oonabe.user.entity.User;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GoogleAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private GoogleTokenVerifier googleTokenVerifier;

    @Test
    void loginWithGoogleRegistersNewUserAndLogsIn() throws Exception {
        String testEmail = "googleuser@example.com";
        Map<String, Object> googleMockResponse = new HashMap<>();
        googleMockResponse.put("email", testEmail);
        googleMockResponse.put("email_verified", "true");
        googleMockResponse.put("name", "Google User");

        Mockito.when(googleTokenVerifier.verifyToken("valid-token")).thenReturn(googleMockResponse);

        // Verify user does not exist first
        Optional<User> existing = userRepository.findByUsernameOrEmail(testEmail, testEmail);
        existing.ifPresent(user -> userRepository.delete(user));

        mockMvc.perform(post("/api/v1/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idToken": "valid-token"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not("")))
                .andExpect(jsonPath("$.refreshToken", not("")))
                .andExpect(jsonPath("$.email").value(testEmail))
                .andExpect(jsonPath("$.username").exists());
    }

    @Test
    void loginWithGoogleLogsInExistingUser() throws Exception {
        String testEmail = "existinggoogle@example.com";
        Map<String, Object> googleMockResponse = new HashMap<>();
        googleMockResponse.put("email", testEmail);
        googleMockResponse.put("email_verified", "true");
        googleMockResponse.put("name", "Existing Google User");

        Mockito.when(googleTokenVerifier.verifyToken("existing-token")).thenReturn(googleMockResponse);

        // Ensure user exists
        mockMvc.perform(post("/api/v1/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idToken": "existing-token"
                                }
                                """))
                .andExpect(status().isOk());

        // Perform login again
        mockMvc.perform(post("/api/v1/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idToken": "existing-token"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(testEmail));
    }
}
