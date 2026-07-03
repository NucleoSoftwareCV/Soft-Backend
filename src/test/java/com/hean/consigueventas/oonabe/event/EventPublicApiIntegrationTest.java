package com.hean.consigueventas.oonabe.event;

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
class EventPublicApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicEventDetailReturnsNotFoundProblemWhenEventDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/events/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso no encontrado"))
                .andExpect(jsonPath("$.message").value("Evento no encontrado con ID: 999999"));
    }

    @Test
    void publicEventListingRejectsInvalidFilterRange() throws Exception {
        mockMvc.perform(get("/api/v1/events")
                        .param("hourFrom", "20")
                        .param("hourTo", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Solicitud invalida"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void publicEventListingAcceptsHourFilter() throws Exception {
        mockMvc.perform(get("/api/v1/events")
                        .param("hourFrom", "6")
                        .param("hourTo", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
