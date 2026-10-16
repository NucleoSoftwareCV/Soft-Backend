package com.hean.consigueventas.oonabe.recommendation;

import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import com.hean.consigueventas.oonabe.event.repository.EventOccurrenceRepository;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventRecommendationServiceTest {

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
    }

    @Test
    void shouldScoreBeforePaginatingAndKeepZeroScoreEvents() {
        mockSignals(Set.of(30L), null);

        EventOccurrence earlier = occurrence(
                1L, 101L, 99L, NOW.plusSeconds(60)
        );

        EventOccurrence followed = occurrence(
                2L, 102L, 30L, NOW.plusSeconds(120)
        );

        when(occurrenceRepository.findRecommendationCandidates(NOW, null))
                .thenReturn(List.of(earlier, followed));

        Page<RecommendedEvent> firstPage = service.getRecommendations(
                USERNAME, PageRequest.of(0, 1)
        );

        Page<RecommendedEvent> secondPage = service.getRecommendations(
                USERNAME, PageRequest.of(1, 1)
        );

        assertThat(firstPage.getTotalElements()).isEqualTo(2);

        assertThat(firstPage.getContent())
                .extracting(result -> result.event().getId())
                .containsExactly(2L);

        assertThat(firstPage.getContent().getFirst().score()).isEqualTo(40);

        assertThat(firstPage.getContent().getFirst().reason())
                .isEqualTo("De un profesional que sigues");

        assertThat(secondPage.getContent().getFirst().event().getId())
                .isEqualTo(1L);

        assertThat(secondPage.getContent().getFirst().score()).isZero();
    }

    @Test
    void shouldKeepOnlyTheNearestOccurrenceForEachEvent() {
        mockSignals(Set.of(), null);

        EventOccurrence later = occurrence(
                1L, 102L, 99L, NOW.plusSeconds(120)
        );

        EventOccurrence earlier = occurrence(
                1L, 101L, 99L, NOW.plusSeconds(60)
        );

        when(occurrenceRepository.findRecommendationCandidates(NOW, null))
                .thenReturn(List.of(later, earlier));

        Page<RecommendedEvent> result = service.getRecommendations(
                USERNAME, PageRequest.of(0, 10)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);

        assertThat(result.getContent().getFirst().occurrenceId())
                .isEqualTo(101L);

        assertThat(result.getContent().getFirst().startsAt())
                .isEqualTo(earlier.getStartsAt());
    }

    @Test
    void shouldBreakTiesByDateAndThenEventId() {
        mockSignals(Set.of(), null);

        EventOccurrence second = occurrence(
                2L, 102L, 99L, NOW.plusSeconds(60)
        );

        EventOccurrence third = occurrence(
                3L, 103L, 99L, NOW.plusSeconds(120)
        );

        EventOccurrence first = occurrence(
                1L, 101L, 99L, NOW.plusSeconds(60)
        );

        when(occurrenceRepository.findRecommendationCandidates(NOW, null))
                .thenReturn(List.of(third, second, first));

        Page<RecommendedEvent> result = service.getRecommendations(
                USERNAME, PageRequest.of(0, 10)
        );

        assertThat(result.getContent())
                .extracting(recommendation -> recommendation.event().getId())
                .containsExactly(1L, 2L, 3L);
    }

    @Test
    void shouldNormalizeCityAndReturnEmptyResults() {
        mockSignals(Set.of(), " Lima ");

        when(occurrenceRepository.findRecommendationCandidates(NOW, "Lima"))
                .thenReturn(List.of());

        Page<RecommendedEvent> result = service.getRecommendations(
                USERNAME, PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();

        verify(occurrenceRepository)
                .findRecommendationCandidates(NOW, "Lima");
    }

    @Test
    void shouldReturnEmptyContentForPageBeyondResults() {
        mockSignals(Set.of(), null);

        when(occurrenceRepository.findRecommendationCandidates(NOW, null))
                .thenReturn(List.of(
                        occurrence(1L, 101L, 99L, NOW.plusSeconds(60))
                ));

        Page<RecommendedEvent> result = service.getRecommendations(
                USERNAME, PageRequest.of(5, 10)
        );

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldRejectExternalSort() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getRecommendations(
                        USERNAME,
                        PageRequest.of(0, 10, Sort.by("title"))
                )
        );

        verifyNoInteractions(signalsService, occurrenceRepository);
    }

    private void mockSignals(
            Set<Long> followedSpecialistIds,
            String cityName
    ) {
        when(signalsService.getSignals(USERNAME))
                .thenReturn(new RecommendationSignals(
                        Set.of(),
                        Set.of(),
                        Set.of(),
                        followedSpecialistIds,
                        null,
                        cityName
                ));
    }

    private EventOccurrence occurrence(
            Long eventId,
            Long occurrenceId,
            Long specialistId,
            Instant startsAt
    ) {
        SpecialistProfile specialist = new SpecialistProfile();
        specialist.setId(specialistId);

        Event event = new Event();
        event.setId(eventId);
        event.setSpecialist(specialist);
        event.setModality(EventModality.ONLINE);

        EventOccurrence occurrence = new EventOccurrence();
        occurrence.setId(occurrenceId);
        occurrence.setEvent(event);
        occurrence.setStartsAt(startsAt);

        return occurrence;
    }
}