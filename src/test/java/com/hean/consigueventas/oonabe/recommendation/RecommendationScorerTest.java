package com.hean.consigueventas.oonabe.recommendation;

import com.hean.consigueventas.oonabe.category.entity.Category;
import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.experienceType.entity.ExperienceType;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RecommendationScorerTest {

    private final RecommendationScorer scorer = new RecommendationScorer();

    @ParameterizedTest
    @CsvSource({
            "false,false,false,false,false,0,Sin coincidencias",
            "true,false,false,false,false,40,De un profesional que sigues",
            "false,true,false,false,false,30,Relacionado con tus intereses",
            "false,false,true,false,false,15,Relacionado con tus favoritos",
            "false,false,false,true,false,10,Según tus preferencias",
            "false,false,false,false,true,5,Según tus preferencias",
            "true,true,true,true,true,100,De un profesional que sigues",
            "false,true,true,true,true,60,Relacionado con tus intereses",
            "false,false,true,true,true,30,Relacionado con tus favoritos",
            "false,false,false,true,true,15,Según tus preferencias",
            "true,true,false,false,true,75,De un profesional que sigues"
    })
    void shouldCalculateScoreAndPrioritizeReason(
            boolean followed,
            boolean interest,
            boolean favorite,
            boolean experience,
            boolean modality,
            int expectedScore,
            String expectedReason
    ) {
        Event event = createEvent();

        RecommendationSignals signals = new RecommendationSignals(
                interest ? Set.of(10L) : Set.of(),
                favorite ? Set.of(10L) : Set.of(),
                experience ? Set.of(20L) : Set.of(),
                followed ? Set.of(30L) : Set.of(),
                modality ? EventModality.ONLINE : null,
                "Lima"
        );

        RecommendationScore result = scorer.score(event, signals);

        assertEquals(expectedScore, result.score());
        assertEquals(expectedReason, result.reason());
    }

    @Test
    void shouldNotScoreDifferentIdsOrModality() {
        RecommendationSignals signals = new RecommendationSignals(
                Set.of(99L),
                Set.of(99L),
                Set.of(99L),
                Set.of(99L),
                EventModality.PRESENCIAL,
                "Lima"
        );

        RecommendationScore result = scorer.score(createEvent(), signals);

        assertEquals(
                new RecommendationScore(0, "Sin coincidencias"),
                result
        );
    }

    @Test
    void shouldTreatNullSetsAsEmpty() {
        RecommendationSignals signals = new RecommendationSignals(
                null, null, null, null, null, null
        );

        RecommendationScore result = scorer.score(createEvent(), signals);

        assertEquals(
                new RecommendationScore(0, "Sin coincidencias"),
                result
        );
    }

    @Test
    void shouldHandleMissingEventRelations() {
        Event event = new Event();

        RecommendationSignals signals = new RecommendationSignals(
                Set.of(10L),
                Set.of(10L),
                Set.of(20L),
                Set.of(30L),
                EventModality.ONLINE,
                "Lima"
        );

        assertEquals(
                new RecommendationScore(0, "Sin coincidencias"),
                scorer.score(event, signals)
        );

        event.setCategory(new Category());
        event.setSpecialist(new SpecialistProfile());
        event.setExperienceType(new ExperienceType());

        assertEquals(
                new RecommendationScore(0, "Sin coincidencias"),
                scorer.score(event, signals)
        );
    }

    @Test
    void shouldNotUseCityToChangeScore() {
        Event event = createEvent();

        RecommendationSignals lima = new RecommendationSignals(
                Set.of(10L), Set.of(), Set.of(), Set.of(), null, "Lima"
        );

        RecommendationSignals cusco = new RecommendationSignals(
                Set.of(10L), Set.of(), Set.of(), Set.of(), null, "Cusco"
        );

        assertEquals(
                scorer.score(event, lima),
                scorer.score(event, cusco)
        );
    }

    @Test
    void shouldRejectNullEvent() {
        RecommendationSignals signals = new RecommendationSignals(
                null, null, null, null, null, null
        );

        assertThrows(
                NullPointerException.class,
                () -> scorer.score(null, signals)
        );
    }

    @Test
    void shouldRejectNullSignals() {
        assertThrows(
                NullPointerException.class,
                () -> scorer.score(createEvent(), null)
        );
    }

    private Event createEvent() {
        Category category = new Category();
        category.setId(10L);

        ExperienceType experienceType = new ExperienceType();
        experienceType.setId(20L);

        SpecialistProfile specialist = new SpecialistProfile();
        specialist.setId(30L);

        Event event = new Event();
        event.setCategory(category);
        event.setExperienceType(experienceType);
        event.setSpecialist(specialist);
        event.setModality(EventModality.ONLINE);

        return event;
    }
}