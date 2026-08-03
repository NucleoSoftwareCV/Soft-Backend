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
                .andExpect(jsonPath("$.paths['/api/auth/login'].post.summary").exists())
                .andExpect(jsonPath("$.paths['/api/auth/google'].post.summary").exists())
                .andExpect(jsonPath("$.paths['/api/v1/auth/admin/login'].post.summary").exists())
                .andExpect(jsonPath("$.paths['/api/v1/categories'].get.summary").exists())
                .andExpect(jsonPath("$.paths['/api/v1/event-occurrences'].get.summary").exists())
                .andExpect(jsonPath("$.paths['/api/v1/cities'].get.summary").exists())
                .andExpect(jsonPath("$.paths['/api/users/me'].get.security[0].bearerAuth").exists());
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
                .andExpect(jsonPath("$.paths['/api/v1/events/{id}'].get.security").isEmpty())
                .andExpect(jsonPath("$.paths['/api/v1/events/{id}/similar'].get.security").isArray())
                .andExpect(jsonPath("$.paths['/api/v1/events/{id}/similar'].get.security").isEmpty())
                .andExpect(jsonPath("$.paths['/api/v1/events/{id}/organizer-events'].get.security").isArray())
                .andExpect(jsonPath("$.paths['/api/v1/events/{id}/organizer-events'].get.security").isEmpty())
                .andExpect(jsonPath("$.paths['/api/v1/home/event-sections'].get.security").isArray())
                .andExpect(jsonPath("$.paths['/api/v1/home/event-sections'].get.security").isEmpty())
                .andExpect(jsonPath("$.components.schemas.EventDetailResponse.properties.includes").exists())
                .andExpect(jsonPath("$.components.schemas.EventDetailResponse.properties.highlights").exists())
                .andExpect(jsonPath("$.components.schemas.EventDetailResponse.properties.whatToBring").exists())
                .andExpect(jsonPath("$.components.schemas.EventOrganizerResponse.properties.whatsappPhone").exists());
    }

    @Test
    void publicOpenApiDoesNotRequireJwtForOneToOnePublicListing() throws Exception {
        mockMvc.perform(get("/v3/api-docs/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/one-to-one-services'].get.security").isArray())
                .andExpect(jsonPath("$.paths['/api/v1/one-to-one-services'].get.security").isEmpty());
    }

    @Test
    void protectedOpenApiDocumentsCommunityMatchRequest() throws Exception {
        mockMvc.perform(get("/v3/api-docs/protected"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/community/match-request'].put.summary").exists())
                .andExpect(jsonPath("$.paths['/api/v1/community/match-request'].put.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/api/v1/community/match-request'].get").doesNotExist());
    }

    @Test
    void protectedOpenApiDocumentsConcurrentSessionUpdate() throws Exception {
        mockMvc.perform(get("/v3/api-docs/protected"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/one-to-one-services/{id}'].put.responses['409']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/one-to-one-services/{id}/status'].patch.responses['409']").exists());
    }

    @Test
    void protectedOpenApiDocumentsProfessionalFollowing() throws Exception {
        mockMvc.perform(get("/v3/api-docs/protected"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/interactions/me/professional-follows'].get.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/api/v1/interactions/me/professional-follows/{professionalId}'].put.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/api/v1/interactions/me/professional-follows/{professionalId}'].delete.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/api/v1/interactions/me/professional-follows/{professionalId}/status'].get.security[0].bearerAuth").exists());
    }

    @Test
    void protectedOpenApiDocumentsProfessionalApplications() throws Exception {
        mockMvc.perform(get("/v3/api-docs/protected"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/professional-applications/me'].put.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/api/v1/professional-applications/me'].get.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/api/v1/admin/professional-applications'].get.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/api/v1/admin/professional-applications/{id}/decision'].patch.security[0].bearerAuth").exists());
    }

    @Test
    void protectedOpenApiDocumentsProfessionalEventManagement() throws Exception {
        mockMvc.perform(get("/v3/api-docs/protected"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/events/my-events'].get.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/api/v1/events/{id}/management'].get.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/api/v1/events/{id}'].put.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/api/v1/events/{id}/status'].patch.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/api/v1/events/{id}/occurrences'].post.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/api/v1/event-occurrences/{id}'].put.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/api/v1/event-occurrences/{id}/status'].patch.security[0].bearerAuth").exists());
    }

    @Test
    void protectedOpenApiDocumentsErpLoginOutsidePublicGroup() throws Exception {
        mockMvc.perform(get("/v3/api-docs/protected"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/auth/admin/login'].post.summary").exists())
                .andExpect(jsonPath("$.paths['/api/v1/auth/admin/login'].post.security").isArray())
                .andExpect(jsonPath("$.paths['/api/v1/auth/admin/login'].post.security").isEmpty());

        mockMvc.perform(get("/v3/api-docs/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/auth/admin/login']").doesNotExist());
    }
}
