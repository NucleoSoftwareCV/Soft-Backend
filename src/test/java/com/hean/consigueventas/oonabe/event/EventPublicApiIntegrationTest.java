package com.hean.consigueventas.oonabe.event;

import com.hean.consigueventas.oonabe.event.repository.EventRepository;
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

    @Autowired
    private EventRepository eventRepository;

    @Test
    void publicEventDetailReturnsOptionalDetailSections() throws Exception {
        Long eventId = eventRepository.findByTitle("Clase Especial de Yoga Vinyasa al Aire Libre")
                .orElseThrow()
                .getId();

        mockMvc.perform(get("/api/v1/events/{id}", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.includes").isArray())
                .andExpect(jsonPath("$.includes[0]").value("Material para practicar yoga"))
                .andExpect(jsonPath("$.highlights").isArray())
                .andExpect(jsonPath("$.highlights[0]").value("Yoga"))
                .andExpect(jsonPath("$.whatToBring").isArray())
                .andExpect(jsonPath("$.whatToBring[0]").value("Ropa comoda"));
    }

    @Test
    void publicEventListingDoesNotExposeDetailSections() throws Exception {
        mockMvc.perform(get("/api/v1/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].includes").doesNotExist())
                .andExpect(jsonPath("$.content[0].highlights").doesNotExist())
                .andExpect(jsonPath("$.content[0].whatToBring").doesNotExist());
    }

    @Test
    void publicSimilarEventsReturnsSameCategoryFromOtherOrganizers() throws Exception {
        var event = eventRepository.findByTitle("Iniciación a la Meditación Trascendental y del Sonido")
                .orElseThrow();

        mockMvc.perform(get("/api/v1/events/{id}/similar", event.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(eventRepository
                        .findByTitle("Retiro Urbano de Mindfulness y Naturaleza")
                        .orElseThrow()
                        .getId()))
                .andExpect(jsonPath("$.content[0].categoryId").value(event.getCategory().getId()))
                .andExpect(jsonPath("$.content[0].organizerId")
                        .value(org.hamcrest.Matchers.not(event.getSpecialist().getId().intValue())));
    }

    @Test
    void publicOrganizerEventsReturnsOtherEventsFromSameOrganizerAndRespectsSize() throws Exception {
        var event = eventRepository.findByTitle("Iniciación a la Meditación Trascendental y del Sonido")
                .orElseThrow();

        mockMvc.perform(get("/api/v1/events/{id}/organizer-events", event.getId())
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(org.hamcrest.Matchers.not(event.getId().intValue())))
                .andExpect(jsonPath("$.content[0].organizerId").value(event.getSpecialist().getId()));
    }

    @Test
    void publicRelatedEventEndpointsReturnNotFoundWhenBaseEventDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/events/999999/similar"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Evento no encontrado con ID: 999999"));

        mockMvc.perform(get("/api/v1/events/999999/organizer-events"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Evento no encontrado con ID: 999999"));
    }

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
