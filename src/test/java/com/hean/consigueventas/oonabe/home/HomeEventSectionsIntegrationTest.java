package com.hean.consigueventas.oonabe.home;

import com.hean.consigueventas.oonabe.category.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(HomeEventSectionsIntegrationTest.FixedClockConfig.class)
class HomeEventSectionsIntegrationTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-07-27T08:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void publicHomeReturnsOrderedNonEmptySectionsWithLimitedCardsAndCovers() throws Exception {
        mockMvc.perform(get("/api/v1/home/event-sections")
                        .param("cityName", "Valencia")
                        .param("limit", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cityName").value("Valencia"))
                .andExpect(jsonPath("$.sections", hasSize(7)))
                .andExpect(jsonPath("$.sections[0].key").value("THIS_WEEK"))
                .andExpect(jsonPath("$.sections[1].key").value("WEEKEND"))
                .andExpect(jsonPath("$.sections[2].key").value("YOGA"))
                .andExpect(jsonPath("$.sections[3].key").value("BREATHWORK_ICE"))
                .andExpect(jsonPath("$.sections[4].key").value("SLOW_DOWN"))
                .andExpect(jsonPath("$.sections[5].key").value("WORKSHOPS"))
                .andExpect(jsonPath("$.sections[6].key").value("RETREATS"))
                .andExpect(jsonPath("$.sections[*].events.length()", everyItem(lessThanOrEqualTo(4))))
                .andExpect(jsonPath("$.sections[*].events[*].coverImageUrl",
                        everyItem(not(nullValue()))))
                .andExpect(jsonPath("$.sections[*].events[*].cityName", not(hasItem("Barcelona"))))
                .andExpect(jsonPath("$.sections[*].events[*].cityName", not(hasItem("Madrid"))))
                .andExpect(jsonPath("$.sections[0].events[0].endsAt").doesNotExist())
                .andExpect(jsonPath("$.sections[0].events[0].eventType").doesNotExist())
                .andExpect(jsonPath("$.sections[0].events[0].isRecurring").doesNotExist())
                .andExpect(jsonPath("$.sections[0].events[0].summary").doesNotExist())
                .andExpect(jsonPath("$.sections[0].events[0].categoryId").doesNotExist())
                .andExpect(jsonPath("$.sections[0].events[0].categoryName").doesNotExist())
                .andExpect(jsonPath("$.sections[0].events[0].organizerId").doesNotExist());
    }

    @Test
    void homeDateSectionsExposeReusableViewAllFilters() throws Exception {
        mockMvc.perform(get("/api/v1/home/event-sections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sections[0].viewAllFilters.dateFrom").value("2026-07-27"))
                .andExpect(jsonPath("$.sections[0].viewAllFilters.dateTo").value("2026-08-02"))
                .andExpect(jsonPath("$.sections[0].viewAllFilters.cityName").value("Valencia"))
                .andExpect(jsonPath("$.sections[0].viewAllFilters.includeOnline").value(true))
                .andExpect(jsonPath("$.sections[1].viewAllFilters.dateFrom").value("2026-07-31"))
                .andExpect(jsonPath("$.sections[1].viewAllFilters.dateTo").value("2026-08-02"));
    }

    @Test
    void publicEventListingSupportsMultipleCategoriesAndCityPlusOnline() throws Exception {
        Long yogaId = categoryRepository.findBySlugAndActiveTrue("yoga").orElseThrow().getId();
        Long breathworkId = categoryRepository.findBySlugAndActiveTrue("hielo-y-breathwork")
                .orElseThrow()
                .getId();

        mockMvc.perform(get("/api/v1/events")
                        .param("categoryIds", yogaId + "," + breathworkId)
                        .param("cityName", "Valencia")
                        .param("includeOnline", "true")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isNotEmpty())
                .andExpect(jsonPath("$.content[*].categoryId",
                        everyItem(org.hamcrest.Matchers.isIn(java.util.List.of(
                                yogaId.intValue(),
                                breathworkId.intValue()
                        )))))
                .andExpect(jsonPath("$.content[*].coverImageUrl",
                        everyItem(not(nullValue()))));
    }

    @Test
    void publicEventListingRejectsSingularAndMultipleCategoryFiltersTogether() throws Exception {
        Long yogaId = categoryRepository.findBySlugAndActiveTrue("yoga").orElseThrow().getId();

        mockMvc.perform(get("/api/v1/events")
                        .param("categoryId", yogaId.toString())
                        .param("categoryIds", yogaId.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Solicitud invalida"));
    }

    @Test
    void homeRejectsLimitOutsideAllowedRange() throws Exception {
        mockMvc.perform(get("/api/v1/home/event-sections").param("limit", "13"))
                .andExpect(status().isBadRequest());
    }

    @TestConfiguration
    static class FixedClockConfig {

        @Bean
        @Primary
        Clock fixedBusinessClock() {
            return Clock.fixed(FIXED_NOW, ZoneId.of("Europe/Madrid"));
        }
    }
}
