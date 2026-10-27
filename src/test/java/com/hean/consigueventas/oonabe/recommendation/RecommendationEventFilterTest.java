package com.hean.consigueventas.oonabe.recommendation;

import com.hean.consigueventas.oonabe.category.entity.Category;
import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.common.enums.EventMood;
import com.hean.consigueventas.oonabe.event.dto.request.EventFilterRequest;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import com.hean.consigueventas.oonabe.experienceType.entity.ExperienceType;
import com.hean.consigueventas.oonabe.masterdata.entity.City;
import com.hean.consigueventas.oonabe.masterdata.entity.Location;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RecommendationEventFilterTest {

    private final RecommendationEventFilter matcher =
            new RecommendationEventFilter();

    private Event event;
    private EventOccurrence occurrence;

    @BeforeEach
    void setUp() {
        Category category = new Category();
        category.setId(10L);

        ExperienceType type = new ExperienceType();
        type.setId(20L);

        SpecialistProfile specialist = new SpecialistProfile();
        specialist.setId(30L);

        City city = new City();
        city.setName("Valencia");

        Location location = new Location();
        location.setCity(city);

        event = new Event();
        event.setTitle("Yoga al aire libre");
        event.setSummary("Movimiento y bienestar");
        event.setCategory(category);
        event.setExperienceType(type);
        event.setSpecialist(specialist);
        event.setModality(EventModality.PRESENCIAL);
        event.setPriceFrom(new BigDecimal("20.00"));
        event.setRecurring(true);

        occurrence = new EventOccurrence();
        occurrence.setEvent(event);
        occurrence.setLocation(location);

        // En Madrid corresponde a las 18:00 del 20 de octubre.
        occurrence.setStartsAt(
                Instant.parse("2026-10-20T16:00:00Z")
        );
    }

    @Test
    void shouldAcceptOccurrenceWhenNoFiltersAreProvided() {
        assertThat(matcher.matches(occurrence, null)).isTrue();

        assertThat(matcher.matches(occurrence, new Filters().build()))
                .isTrue();
    }

    @Test
    void shouldSearchTitleAndSummaryIgnoringCaseAndOuterSpaces() {
        Filters filters = new Filters();

        filters.search = "  YOGA  ";
        assertThat(matcher.matches(occurrence, filters.build())).isTrue();

        filters.search = "BIENESTAR";
        assertThat(matcher.matches(occurrence, filters.build())).isTrue();

        filters.search = "natacion";
        assertThat(matcher.matches(occurrence, filters.build())).isFalse();
    }

    @Test
    void shouldFilterBySingleCategoryOrMultipleCategories() {
        Filters filters = new Filters();

        filters.categoryId = 10L;
        assertThat(matcher.matches(occurrence, filters.build())).isTrue();

        filters.categoryId = 99L;
        assertThat(matcher.matches(occurrence, filters.build())).isFalse();

        filters.categoryId = null;
        filters.categoryIds = List.of(99L, 10L);
        assertThat(matcher.matches(occurrence, filters.build())).isTrue();

        filters.categoryIds = List.of(98L, 99L);
        assertThat(matcher.matches(occurrence, filters.build())).isFalse();
    }

    @Test
    void shouldCombineExperienceModalitySpecialistAndRecurrence() {
        Filters filters = new Filters();
        filters.experienceTypeId = 20L;
        filters.modality = EventModality.PRESENCIAL;
        filters.specialistId = 30L;
        filters.isRecurring = true;

        assertThat(matcher.matches(occurrence, filters.build())).isTrue();

        filters.experienceTypeId = 99L;
        assertThat(matcher.matches(occurrence, filters.build())).isFalse();
        filters.experienceTypeId = 20L;

        filters.modality = EventModality.ONLINE;
        assertThat(matcher.matches(occurrence, filters.build())).isFalse();
        filters.modality = EventModality.PRESENCIAL;

        filters.specialistId = 99L;
        assertThat(matcher.matches(occurrence, filters.build())).isFalse();
        filters.specialistId = 30L;

        filters.isRecurring = false;
        assertThat(matcher.matches(occurrence, filters.build())).isFalse();
    }

    @Test
    void shouldRespectCityAndExplicitOnlineInclusion() {
        Filters filters = new Filters();
        filters.cityName = " valencia ";

        assertThat(matcher.matches(occurrence, filters.build())).isTrue();

        filters.cityName = "Madrid";
        assertThat(matcher.matches(occurrence, filters.build())).isFalse();

        event.setModality(EventModality.ONLINE);

        filters.includeOnline = true;
        assertThat(matcher.matches(occurrence, filters.build())).isTrue();

        filters.includeOnline = false;
        assertThat(matcher.matches(occurrence, filters.build())).isFalse();
    }

    @Test
    void shouldUseInclusivePriceBoundsAndRejectUnknownPrice() {
        Filters filters = new Filters();
        filters.minPrice = new BigDecimal("20");
        filters.maxPrice = new BigDecimal("20");

        assertThat(matcher.matches(occurrence, filters.build())).isTrue();

        filters.maxPrice = new BigDecimal("19");
        filters.minPrice = null;
        assertThat(matcher.matches(occurrence, filters.build())).isFalse();

        event.setPriceFrom(null);
        assertThat(matcher.matches(occurrence, filters.build())).isFalse();

        filters.maxPrice = null;
        assertThat(matcher.matches(occurrence, filters.build())).isTrue();
    }

    @Test
    void shouldUseInclusiveDatesAndBusinessTimezone() {
        Filters filters = new Filters();
        filters.dateFrom = LocalDate.of(2026, 10, 20);
        filters.dateTo = LocalDate.of(2026, 10, 20);

        assertThat(matcher.matches(occurrence, filters.build())).isTrue();

        occurrence.setStartsAt(
                Instant.parse("2026-10-20T22:30:00Z")
        );

        // En Madrid ya es el 21 de octubre.
        assertThat(matcher.matches(occurrence, filters.build())).isFalse();
    }

    @Test
    void shouldUseBusinessHourForTimeFilters() {
        Filters filters = new Filters();
        filters.hourFrom = 18;
        filters.hourTo = 18;

        assertThat(matcher.matches(occurrence, filters.build())).isTrue();

        filters.hourFrom = 16;
        filters.hourTo = 17;

        assertThat(matcher.matches(occurrence, filters.build())).isFalse();
    }

    @Test
    void shouldRequireCityAndDateToMatchTheSameOccurrence() {
        Filters filters = new Filters();
        filters.cityName = "Valencia";
        filters.dateFrom = LocalDate.of(2026, 10, 21);

        assertThat(matcher.matches(occurrence, filters.build())).isFalse();

        filters.dateFrom = LocalDate.of(2026, 10, 20);
        filters.cityName = "Madrid";

        assertThat(matcher.matches(occurrence, filters.build())).isFalse();

        filters.cityName = "Valencia";

        assertThat(matcher.matches(occurrence, filters.build())).isTrue();
    }

    @Test
    void shouldFilterEachMoodAndExcludeUnclassifiedEventsOnlyWhenRequested() {
        Filters filters = new Filters();
        for (EventMood mood : EventMood.values()) {
            filters.mood = mood;
            event.setMood(mood);
            assertThat(matcher.matches(occurrence, filters.build())).isTrue();
            event.setMood(null);
            assertThat(matcher.matches(occurrence, filters.build())).isFalse();
        }
        filters.mood = null;
        assertThat(matcher.matches(occurrence, filters.build())).isTrue();
        filters.mood = EventMood.ENERGIA;
        event.setMood(EventMood.RELAJACION);
        assertThat(matcher.matches(occurrence, filters.build())).isFalse();
    }

    private static class Filters {
        String search;
        Long categoryId;
        List<Long> categoryIds;
        Long experienceTypeId;
        EventModality modality;
        String cityName;
        Boolean includeOnline;
        BigDecimal minPrice;
        BigDecimal maxPrice;
        LocalDate dateFrom;
        LocalDate dateTo;
        Integer hourFrom;
        Integer hourTo;
        Boolean isRecurring;
        Long specialistId;
        EventMood mood;

        EventFilterRequest build() {
            return new EventFilterRequest(
                    search,
                    categoryId,
                    categoryIds,
                    experienceTypeId,
                    modality,
                    cityName,
                    includeOnline,
                    minPrice,
                    maxPrice,
                    dateFrom,
                    dateTo,
                    hourFrom,
                    hourTo,
                    isRecurring,
                    specialistId,
                    mood
            );
        }
    }
}