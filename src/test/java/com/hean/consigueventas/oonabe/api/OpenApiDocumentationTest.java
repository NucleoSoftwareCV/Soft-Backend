package com.hean.consigueventas.oonabe.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void openApiDocumentsJwtSecurityAndPublicEndpoints() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.paths['/api/v1/auth/login'].post.summary").exists())
                .andExpect(jsonPath("$.paths['/api/v1/auth/google'].post.summary").exists())
                .andExpect(jsonPath("$.paths['/api/v1/categories'].get.summary").exists())
                .andExpect(jsonPath("$.paths['/api/v1/event-occurrences'].get.summary").exists())
                .andExpect(jsonPath("$.paths['/api/v1/cities'].get.summary").exists())
                .andExpect(jsonPath("$.paths['/api/v1/users/me'].get.security[0].bearerAuth").exists());
    }

    @Test
    void publicOpenApiDoesNotRequireJwtForPublicEvents() throws Exception {
        mockMvc.perform(get("/v3/api-docs/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.security").isArray())
                .andExpect(jsonPath("$.security").isEmpty())
                .andExpect(jsonPath("$.paths['/api/v1/events'].get.security").isArray())
                .andExpect(jsonPath("$.paths['/api/v1/events'].get.security").isEmpty())
                .andExpect(jsonPath("$.paths['/api/v1/events/{id}'].get.security").isArray())
                .andExpect(jsonPath("$.paths['/api/v1/events/{id}'].get.security").isEmpty());
    }
}
