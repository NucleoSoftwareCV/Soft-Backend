package com.hean.consigueventas.oonabe.event;

import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.repository.EventOccurrenceRepository;
import com.hean.consigueventas.oonabe.event.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EventOccurrencePublicCalendarIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventOccurrenceRepository occurrenceRepository;

    @Test
    void publicCalendarReturnsOnlyOccurrencesOfRequestedSpecialistInRange() throws Exception {
        Event event = eventRepository.findByTitle("Iniciación a la Meditación Trascendental y del Sonido")
                .orElseThrow();
        Long specialistId = event.getSpecialist().getId();
        var occurrence = occurrenceRepository.findByEventIdOrderByStartsAtAsc(event.getId())
                .stream().findFirst().orElseThrow();

        LocalDate occurrenceDate = occurrence.getStartsAt().atZone(ZoneId.of("Europe/Madrid")).toLocalDate();

        mockMvc.perform(get("/api/v1/event-occurrences/public")
                        .param("specialistId", specialistId.toString())
                        .param("dateFrom", occurrenceDate.toString())
                        .param("dateTo", occurrenceDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].occurrenceId").value(hasItem(occurrence.getId().intValue())))
                .andExpect(jsonPath("$[*].eventId").value(hasItem(event.getId().intValue())));
    }

    @Test
    void publicCalendarReturnsEmptyListWhenNoOccurrencesInRange() throws Exception {
        Event event = eventRepository.findByTitle("Iniciación a la Meditación Trascendental y del Sonido")
                .orElseThrow();
        Long specialistId = event.getSpecialist().getId();

        mockMvc.perform(get("/api/v1/event-occurrences/public")
                        .param("specialistId", specialistId.toString())
                        .param("dateFrom", "2020-01-01")
                        .param("dateTo", "2020-01-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void publicCalendarRequiresQueryParams() throws Exception {
        mockMvc.perform(get("/api/v1/event-occurrences/public"))
                .andExpect(status().isBadRequest());
    }
}
