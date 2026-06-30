package com.hean.consigueventas.oonabe.oneToOneSession;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OneToOnePublicListingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicListingIsPaginatedAndOnlyExposesCardFields() throws Exception {
        mockMvc.perform(get("/api/v1/one-to-one-services")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(12))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.content[0].id").exists())
                .andExpect(jsonPath("$.content[0].title").exists())
                .andExpect(jsonPath("$.content[0].specialistName").exists())
                .andExpect(jsonPath("$.content[0].price").exists())
                .andExpect(jsonPath("$.content[0].currency").exists())
                .andExpect(jsonPath("$.content[0].durationMinutes").exists())
                .andExpect(jsonPath("$.content[0].imageUrl").exists())
                .andExpect(jsonPath("$.content[0]", not(hasKey("slug"))))
                .andExpect(jsonPath("$.content[0]", not(hasKey("description"))))
                .andExpect(jsonPath("$.content[0]", not(hasKey("status"))))
                .andExpect(jsonPath("$.content[0]", not(hasKey("modality"))))
                .andExpect(jsonPath("$.content[0]", not(hasKey("locationName"))))
                .andExpect(jsonPath("$.content[0]", not(hasKey("createdAt"))))
                .andExpect(jsonPath("$.content[0]", not(hasKey("updatedAt"))));
    }

    @Test
    void secondPageUsesRequestedSizeAndOnlyPublishedSessions() throws Exception {
        mockMvc.perform(get("/api/v1/one-to-one-services")
                        .param("page", "1")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.totalElements").value(12))
                .andExpect(jsonPath("$.size").value(3))
                .andExpect(jsonPath("$.number").value(1));
    }

    @Test
    void swaggerExampleSortIsIgnoredInsteadOfFailing() throws Exception {
        mockMvc.perform(get("/api/v1/one-to-one-services")
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "[\"string\"]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(12));
    }
}
