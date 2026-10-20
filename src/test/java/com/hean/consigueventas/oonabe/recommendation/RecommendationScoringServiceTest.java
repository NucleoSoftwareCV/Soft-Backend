package com.hean.consigueventas.oonabe.recommendation;

import com.hean.consigueventas.oonabe.category.entity.Category;
import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.experienceType.entity.ExperienceType;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecommendationScoringServiceTest {

    private final RecommendationScoringService service = new RecommendationScoringService();

    @Test
    void returnsOneHundredPointsAndThePriorityReasonWhenEveryCriterionMatches() {
        Event event = event(1L, 10L, 20L, 30L, EventModality.ONLINE);
        RecommendationSignals signals = new RecommendationSignals(
                Set.of(20L), Set.of(20L), Set.of(30L), Set.of(10L), EventModality.ONLINE, "Valencia");

        var results = service.scoreAndSort(List.of(event), signals);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().event()).isSameAs(event);
        assertThat(results.getFirst().score())
                .isEqualTo(new RecommendationScore(100, "De un profesional que sigues"));
    }

    @Test
    void ordersCandidatesByDescendingScoreAndPreservesTheirReasons() {
        Event modality = event(1L, 11L, 21L, 31L, EventModality.ONLINE);
        Event interest = event(2L, 12L, 20L, 32L, EventModality.PRESENCIAL);
        Event followed = event(3L, 10L, 23L, 33L, EventModality.PRESENCIAL);
        Event favorite = event(4L, 14L, 24L, 34L, EventModality.PRESENCIAL);
        Event experience = event(5L, 15L, 25L, 30L, EventModality.PRESENCIAL);
        RecommendationSignals signals = new RecommendationSignals(
                Set.of(20L), Set.of(24L), Set.of(30L), Set.of(10L), EventModality.ONLINE, null);

        var results = service.scoreAndSort(List.of(modality, interest, followed, favorite, experience), signals);

        assertThat(results).extracting(RecommendationScoringService.ScoredEvent::event)
                .containsExactly(followed, interest, favorite, experience, modality);
        assertThat(results).extracting(RecommendationScoringService.ScoredEvent::score)
                .containsExactly(
                        new RecommendationScore(40, "De un profesional que sigues"),
                        new RecommendationScore(30, "Relacionado con tus intereses"),
                        new RecommendationScore(15, "Relacionado con tus favoritos"),
                        new RecommendationScore(10, "Según tus preferencias"),
                        new RecommendationScore(5, "Según tus preferencias"));
    }

    @Test
    void preservesInputOrderForTiedScores() {
        Event first = event(30L, 10L, 20L, null, EventModality.ONLINE);
        Event second = event(10L, 11L, 20L, null, EventModality.ONLINE);
        Event third = event(20L, 12L, 21L, null, EventModality.ONLINE);
        RecommendationSignals signals = new RecommendationSignals(
                Set.of(20L), Set.of(), Set.of(), Set.of(), null, null);

        var results = service.scoreAndSort(List.of(third, first, second), signals);

        assertThat(results).extracting(RecommendationScoringService.ScoredEvent::event)
                .containsExactly(first, second, third);
        assertThat(results).extracting(result -> result.score().score()).containsExactly(30, 30, 0);
    }

    @Test
    void returnsZeroAndPreservesInputOrderWithoutPreferences() {
        Event first = event(20L, 10L, 20L, 30L, EventModality.ONLINE);
        Event second = event(10L, 11L, 21L, null, EventModality.HIBRIDA);
        RecommendationSignals signals = new RecommendationSignals(null, null, null, null, null, "Valencia");

        var results = service.scoreAndSort(List.of(first, second), signals);

        assertThat(results).extracting(RecommendationScoringService.ScoredEvent::event)
                .containsExactly(first, second);
        assertThat(results).extracting(RecommendationScoringService.ScoredEvent::score)
                .containsOnly(new RecommendationScore(0, "Sin coincidencias"));
    }

    @Test
    void accumulatesOnlyAvailableMatchingSignals() {
        Event event = event(1L, 10L, 20L, 30L, EventModality.ONLINE);
        RecommendationSignals signals = new RecommendationSignals(
                Set.of(20L), null, Set.of(30L), null, null, null);

        var result = service.scoreAndSort(List.of(event), signals).getFirst();

        assertThat(result.score()).isEqualTo(new RecommendationScore(40, "Relacionado con tus intereses"));
    }

    @Test
    void doesNotAddExperiencePointsWhenTheEventHasNoExperienceType() {
        Event event = event(1L, 10L, 20L, null, EventModality.ONLINE);
        RecommendationSignals signals = new RecommendationSignals(
                Set.of(), Set.of(), Set.of(30L), Set.of(), EventModality.ONLINE, null);

        var result = service.scoreAndSort(List.of(event), signals).getFirst();

        assertThat(result.score()).isEqualTo(new RecommendationScore(5, "Según tus preferencias"));
    }

    @Test
    void matchesHybridModalityOnlyWhenItIsTheExactPreference() {
        Event hybrid = event(1L, 10L, 20L, null, EventModality.HIBRIDA);
        RecommendationSignals onlineSignals = new RecommendationSignals(
                Set.of(), Set.of(), Set.of(), Set.of(), EventModality.ONLINE, null);
        RecommendationSignals hybridSignals = new RecommendationSignals(
                Set.of(), Set.of(), Set.of(), Set.of(), EventModality.HIBRIDA, null);

        assertThat(service.scoreAndSort(List.of(hybrid), onlineSignals).getFirst().score().score()).isZero();
        assertThat(service.scoreAndSort(List.of(hybrid), hybridSignals).getFirst().score().score()).isEqualTo(5);
    }

    @Test
    void preservesTheOriginalListAndEntitiesWhileKeepingEveryCandidate() {
        Event first = event(30L, 10L, 20L, 30L, EventModality.ONLINE);
        Event second = event(10L, 11L, 21L, null, EventModality.PRESENCIAL);
        List<Event> candidates = new ArrayList<>(List.of(second, first));
        List<Event> expectedOriginal = List.of(
                event(10L, 11L, 21L, null, EventModality.PRESENCIAL),
                event(30L, 10L, 20L, 30L, EventModality.ONLINE));
        RecommendationSignals signals = new RecommendationSignals(
                Set.of(20L), Set.of(), Set.of(), Set.of(), null, null);

        var results = service.scoreAndSort(candidates, signals);

        assertThat(candidates).containsExactly(second, first);
        assertThat(candidates).usingRecursiveComparison().isEqualTo(expectedOriginal);
        assertThat(results).extracting(RecommendationScoringService.ScoredEvent::event)
                .containsExactly(first, second);
    }

    @Test
    void returnsAnEmptyListForEmptyCandidates() {
        assertThat(service.scoreAndSort(List.of(), noPreferences())).isEmpty();
    }

    @Test
    void rejectsNullCandidates() {
        assertThatThrownBy(() -> service.scoreAndSort(null, noPreferences()))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Los candidatos son obligatorios");
    }

    @Test
    void rejectsNullSignalsEvenWhenCandidatesAreEmpty() {
        assertThatThrownBy(() -> service.scoreAndSort(List.of(), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Las señales son obligatorias");
    }

    @Test
    void rejectsNullElementsInsteadOfSilentlyDiscardingThem() {
        List<Event> candidates = Arrays.asList(event(1L, 10L, 20L, null, EventModality.ONLINE), null);

        assertThatThrownBy(() -> service.scoreAndSort(candidates, noPreferences()))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("El evento es obligatorio");
    }

    private RecommendationSignals noPreferences() {
        return new RecommendationSignals(Set.of(), Set.of(), Set.of(), Set.of(), null, null);
    }

    private Event event(Long id, Long specialistId, Long categoryId, Long experienceTypeId, EventModality modality) {
        SpecialistProfile specialist = new SpecialistProfile();
        specialist.setId(specialistId);
        Category category = new Category();
        category.setId(categoryId);
        Event event = new Event();
        event.setId(id);
        event.setSpecialist(specialist);
        event.setCategory(category);
        event.setModality(modality);
        if (experienceTypeId != null) {
            ExperienceType experienceType = new ExperienceType();
            experienceType.setId(experienceTypeId);
            event.setExperienceType(experienceType);
        }
        return event;
    }
}
