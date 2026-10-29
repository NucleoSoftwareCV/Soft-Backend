package com.hean.consigueventas.oonabe.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hean.consigueventas.oonabe.common.enums.EventMood;
import com.hean.consigueventas.oonabe.event.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:event_mood_api;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class EventMoodIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired EventRepository events;
    @Autowired @Qualifier("seedBaseData") CommandLineRunner seeder;

    @ParameterizedTest
    @EnumSource(EventMood.class)
    @WithMockUser(username = "user1", roles = "USER")
    void keepsCatalogRecommendationsAndMapConsistentForEachMood(EventMood mood) throws Exception {
        JsonNode catalog = read("/api/v1/events", mood);
        Set<Long> expectedPhysical = new HashSet<>();
        assertThat(catalog.path("totalElements").asLong()).isPositive();
        for (JsonNode card : catalog.path("content")) {
            assertThat(card.path("mood").asText()).isEqualTo(mood.name());
            if (!"ONLINE".equals(card.path("modality").asText())) {
                expectedPhysical.add(card.path("id").asLong());
            }
        }

        JsonNode recommendations = read("/api/v1/recommendations/events", mood);
        assertThat(recommendations.path("totalElements").asLong())
                .isEqualTo(catalog.path("totalElements").asLong());
        for (JsonNode item : recommendations.path("content")) {
            assertThat(item.path("event").path("mood").asText()).isEqualTo(mood.name());
        }

        JsonNode firstPage = json.readTree(mvc.perform(get("/api/v1/recommendations/events")
                        .param("mood", mood.name()).param("size", "1")
                        .param("useProfileCity", "false"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertThat(firstPage.path("content").size()).isEqualTo(1);
        assertThat(firstPage.path("totalElements").asLong())
                .isEqualTo(catalog.path("totalElements").asLong());

        JsonNode map = json.readTree(mvc.perform(get("/api/v1/events/map"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        Set<Long> actualPhysical = new HashSet<>();
        for (JsonNode point : map.path("content")) {
            if (mood.name().equals(point.path("mood").asText())) {
                actualPhysical.add(point.path("eventId").asLong());
            }
        }
        assertThat(actualPhysical).isEqualTo(expectedPhysical);
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void unclassifiedEventsRemainVisibleWithoutMoodButDoNotMatchASpecificMood() throws Exception {
        var event = events.findByTitle("Clase Especial de Yoga Vinyasa al Aire Libre").orElseThrow();
        event.setMood(null);
        events.saveAndFlush(event);

        JsonNode filtered = read("/api/v1/events", EventMood.ENERGIA);
        for (JsonNode card : filtered.path("content")) {
            assertThat(card.path("id").asLong()).isNotEqualTo(event.getId());
        }
        JsonNode recommendations = read("/api/v1/recommendations/events", EventMood.ENERGIA);
        for (JsonNode item : recommendations.path("content")) {
            assertThat(item.path("event").path("id").asLong()).isNotEqualTo(event.getId());
        }
        JsonNode unfiltered = read("/api/v1/events", null);
        Set<Long> ids = new HashSet<>();
        unfiltered.path("content").forEach(card -> ids.add(card.path("id").asLong()));
        assertThat(ids).contains(event.getId());
    }

    @Test
    void restartingSeedsDoesNotReplaceEditedMoodOrCreateMoreEvents() throws Exception {
        var event = events.findByTitle("Yoga Restaurativo para Soltar Tension").orElseThrow();
        event.setMood(EventMood.CONECTAR);
        events.saveAndFlush(event);
        long count = events.count();
        seeder.run();
        assertThat(events.findById(event.getId()).orElseThrow().getMood()).isEqualTo(EventMood.CONECTAR);
        assertThat(events.count()).isEqualTo(count);
    }

    @ParameterizedTest
    @ValueSource(strings = { "/api/v1/events", "/api/v1/recommendations/events" })
    @WithMockUser(username = "user1", roles = "USER")
    void rejectsAnUnknownMood(String path) throws Exception {
        mvc.perform(get(path).param("mood", "INVALIDO"))
                .andExpect(status().isBadRequest());
    }

    private JsonNode read(String path, EventMood mood) throws Exception {
        var request = get(path).param("size", "100").param("useProfileCity", "false");
        if (mood != null) request.param("mood", mood.name());
        return json.readTree(mvc.perform(request).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
    }
}