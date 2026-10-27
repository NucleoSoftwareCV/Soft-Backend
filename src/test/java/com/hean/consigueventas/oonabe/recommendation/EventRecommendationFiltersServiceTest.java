package com.hean.consigueventas.oonabe.recommendation;

import com.hean.consigueventas.oonabe.category.entity.Category;
import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.common.enums.EventMood;
import com.hean.consigueventas.oonabe.event.dto.request.EventFilterRequest;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import com.hean.consigueventas.oonabe.event.repository.EventOccurrenceRepository;
import com.hean.consigueventas.oonabe.masterdata.entity.City;
import com.hean.consigueventas.oonabe.masterdata.entity.Location;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventRecommendationFiltersServiceTest {

    private static final String USERNAME = "cliente";

    private static final Instant NOW =
            Instant.parse("2026-10-16T12:00:00Z");

    @Mock
    private RecommendationSignalsService signalsService;

    @Mock
    private EventOccurrenceRepository occurrenceRepository;

    private EventRecommendationService service;

    @BeforeEach
    void setUp() {
        service = new EventRecommendationService(
                signalsService,
                occurrenceRepository,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );

        when(signalsService.getSignals(USERNAME))
                .thenReturn(new RecommendationSignals(
                        Set.of(),
                        Set.of(),
                        Set.of(),
                        Set.of(30L),
                        null,
                        "Valencia"
                ));
    }

    @Test
    void shouldPrioritizeExplicitCityOverProfileCity() {
        EventOccurrence occurrence = occurrence(
                1L, 101L, 10L, 30L,
                "Barcelona", "2026-10-20T12:00:00Z"
        );

        when(occurrenceRepository.findRecommendationCandidates(NOW, "Barcelona"))
                .thenReturn(List.of(occurrence));

        var result = service.getRecommendations(
                USERNAME,
                PageRequest.of(0, 12),
                filter(null, " Barcelona ", true, null),
                true
        );

        assertThat(result.getContent())
                .extracting(RecommendedEvent::cityName)
                .containsExactly("Barcelona");

        verify(occurrenceRepository)
                .findRecommendationCandidates(NOW, "Barcelona");
    }

    @Test
    void shouldAllowIgnoringProfileCity() {
        when(occurrenceRepository.findRecommendationCandidates(NOW, null))
                .thenReturn(List.of());

        service.getRecommendations(
                USERNAME,
                PageRequest.of(0, 12),
                null,
                false
        );

        verify(occurrenceRepository)
                .findRecommendationCandidates(NOW, null);
    }

    @Test
    void shouldFilterBeforeScoringAndPaginating() {
        EventOccurrence highScoreButWrongCategory = occurrence(
                1L, 101L, 99L, 30L,
                "Valencia", "2026-10-20T12:00:00Z"
        );

        EventOccurrence matchingCategory = occurrence(
                2L, 102L, 10L, 80L,
                "Valencia", "2026-10-21T12:00:00Z"
        );

        when(occurrenceRepository.findRecommendationCandidates(NOW, "Valencia"))
                .thenReturn(List.of(
                        highScoreButWrongCategory,
                        matchingCategory
                ));

        var result = service.getRecommendations(
                USERNAME,
                PageRequest.of(0, 1),
                filter(10L, null, true, null),
                true
        );

        assertThat(result.getTotalElements()).isEqualTo(1);

        assertThat(result.getContent())
                .extracting(item -> item.event().getId())
                .containsExactly(2L);

        assertThat(result.getContent().getFirst().score()).isZero();
    }

    @Test
    void shouldSelectTheNearestOccurrenceThatMatchesTheDateFilter() {
        EventOccurrence earlier = occurrence(
                1L, 101L, 10L, 30L,
                "Valencia", "2026-10-17T12:00:00Z"
        );

        EventOccurrence later = occurrence(
                1L, 102L, 10L, 30L,
                "Valencia", "2026-10-21T12:00:00Z"
        );

        when(occurrenceRepository.findRecommendationCandidates(NOW, "Valencia"))
                .thenReturn(List.of(earlier, later));

        var result = service.getRecommendations(
                USERNAME,
                PageRequest.of(0, 12),
                filter(null, null, true, LocalDate.of(2026, 10, 20)),
                true
        );

        assertThat(result.getTotalElements()).isEqualTo(1);

        assertThat(result.getContent().getFirst().occurrenceId())
                .isEqualTo(102L);

        assertThat(result.getContent().getFirst().startsAt())
                .isEqualTo(later.getStartsAt());
    }

    @Test
    void shouldNotCombineCityAndDateFromDifferentOccurrences() {
        EventOccurrence cityMatchesButDateDoesNot = occurrence(
                1L, 101L, 10L, 30L,
                "Valencia", "2026-10-17T12:00:00Z"
        );

        EventOccurrence dateMatchesButCityDoesNot = occurrence(
                1L, 102L, 10L, 30L,
                "Barcelona", "2026-10-21T12:00:00Z"
        );

        when(occurrenceRepository.findRecommendationCandidates(NOW, "Valencia"))
                .thenReturn(List.of(
                        cityMatchesButDateDoesNot,
                        dateMatchesButCityDoesNot
                ));

        var result = service.getRecommendations(
                USERNAME,
                PageRequest.of(0, 12),
                filter(
                        null,
                        "Valencia",
                        false,
                        LocalDate.of(2026, 10, 20)
                ),
                true
        );

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void shouldExcludeOnlineEventsWhenCityFilterDoesNotIncludeThem() {
        EventOccurrence online = occurrence(
                1L, 101L, 10L, 30L,
                "Valencia", "2026-10-20T12:00:00Z"
        );
        online.getEvent().setModality(EventModality.ONLINE);

        when(occurrenceRepository.findRecommendationCandidates(NOW, "Valencia"))
                .thenReturn(List.of(online));

        var result = service.getRecommendations(
                USERNAME,
                PageRequest.of(0, 12),
                filter(null, "Valencia", false, null),
                true
        );

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void shouldFilterMoodBeforeRankingAndPagination() {
        EventOccurrence relaxation = occurrence(1L, 101L, 10L, 30L, "Valencia", "2026-10-20T12:00:00Z");
        EventOccurrence energy = occurrence(2L, 102L, 10L, 99L, "Valencia", "2026-10-20T12:00:00Z");
        EventOccurrence followedEnergy = occurrence(3L, 103L, 10L, 30L, "Valencia", "2026-10-21T12:00:00Z");
        EventOccurrence unknown = occurrence(4L, 104L, 10L, 30L, "Valencia", "2026-10-20T12:00:00Z");
        relaxation.getEvent().setMood(EventMood.RELAJACION);
        energy.getEvent().setMood(EventMood.ENERGIA);
        followedEnergy.getEvent().setMood(EventMood.ENERGIA);
        when(occurrenceRepository.findRecommendationCandidates(NOW, null))
                .thenReturn(List.of(relaxation, energy, unknown, followedEnergy));
        EventFilterRequest filter = new EventFilterRequest(
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, EventMood.ENERGIA);

        var first = service.getRecommendations(USERNAME, PageRequest.of(0, 1), filter, false);
        var second = service.getRecommendations(USERNAME, PageRequest.of(1, 1), filter, false);
        assertThat(first.getTotalElements()).isEqualTo(2);
        assertThat(first.getContent()).extracting(result -> result.event().getId()).containsExactly(3L);
        assertThat(second.getContent()).extracting(result -> result.event().getId()).containsExactly(2L);
    }

    private EventFilterRequest filter(
            Long categoryId,
            String cityName,
            Boolean includeOnline,
            LocalDate dateFrom
    ) {
        return new EventFilterRequest(
                null,
                categoryId,
                null,
                null,
                null,
                cityName,
                includeOnline,
                null,
                null,
                dateFrom,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private EventOccurrence occurrence(
            Long eventId,
            Long occurrenceId,
            Long categoryId,
            Long specialistId,
            String cityName,
            String startsAt
    ) {
        Category category = new Category();
        category.setId(categoryId);

        SpecialistProfile specialist = new SpecialistProfile();
        specialist.setId(specialistId);

        City city = new City();
        city.setName(cityName);

        Location location = new Location();
        location.setCity(city);

        Event event = new Event();
        event.setId(eventId);
        event.setCategory(category);
        event.setSpecialist(specialist);
        event.setModality(EventModality.PRESENCIAL);

        EventOccurrence occurrence = new EventOccurrence();
        occurrence.setId(occurrenceId);
        occurrence.setEvent(event);
        occurrence.setLocation(location);
        occurrence.setStartsAt(Instant.parse(startsAt));
        occurrence.setEndsAt(
                Instant.parse(startsAt).plusSeconds(3600)
        );

        return occurrence;
    }
}