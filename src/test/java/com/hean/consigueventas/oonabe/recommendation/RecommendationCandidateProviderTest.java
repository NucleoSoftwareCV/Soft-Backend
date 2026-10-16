package com.hean.consigueventas.oonabe.recommendation;

import com.hean.consigueventas.oonabe.category.entity.Category;
import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.common.enums.EventOccurrenceStatus;
import com.hean.consigueventas.oonabe.common.enums.EventStatus;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import com.hean.consigueventas.oonabe.experienceType.entity.ExperienceType;
import com.hean.consigueventas.oonabe.masterdata.entity.City;
import com.hean.consigueventas.oonabe.masterdata.entity.Location;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import com.hean.consigueventas.oonabe.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
@ActiveProfiles("test")
@Import({RecommendationCandidateProvider.class, RecommendationCandidateProviderTest.FixedClockConfig.class})
class RecommendationCandidateProviderTest {

    private static final Instant NOW = Instant.parse("2026-10-16T12:00:00Z");
    private static final Instant FUTURE = NOW.plusSeconds(3600);

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RecommendationCandidateProvider provider;

    private Category category;
    private SpecialistProfile specialist;
    private Location valencia;
    private Location madrid;
    private int eventSequence;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setName("Yoga candidatos");
        entityManager.persist(category);

        User user = new User();
        user.setEmail("candidates@example.com");
        entityManager.persist(user);

        specialist = new SpecialistProfile();
        specialist.setUser(user);
        specialist.setSlug("candidate-specialist");
        specialist.setPublicName("Candidate Specialist");
        specialist.setWhatsappPhone("+34600000000");
        entityManager.persist(specialist);

        valencia = location("Valencia");
        madrid = location("Madrid");
    }

    @ParameterizedTest
    @EnumSource(value = EventStatus.class, names = "PUBLICADO", mode = EnumSource.Mode.EXCLUDE)
    void excludesUnpublishedEvents(EventStatus status) {
        Event excluded = event(EventModality.ONLINE);
        excluded.setStatus(status);
        occurrence(excluded, FUTURE, null, EventOccurrenceStatus.PROGRAMADA);
        Event included = eligible(EventModality.ONLINE, null);

        assertThat(candidates("Valencia")).containsExactly(included.getId());
    }

    @Test
    void excludesEventsWithoutFutureOccurrencesAndIncludesExactCurrentInstant() {
        event(EventModality.ONLINE);
        Event past = event(EventModality.ONLINE);
        occurrence(past, NOW.minusSeconds(1), null, EventOccurrenceStatus.PROGRAMADA);
        Event startsNow = event(EventModality.ONLINE);
        occurrence(startsNow, NOW, null, EventOccurrenceStatus.PROGRAMADA);
        Event future = eligible(EventModality.ONLINE, null);

        assertThat(candidates(null)).containsExactly(startsNow.getId(), future.getId());
    }

    @ParameterizedTest
    @EnumSource(value = EventOccurrenceStatus.class, names = "PROGRAMADA", mode = EnumSource.Mode.EXCLUDE)
    void excludesFutureOccurrencesThatAreNotScheduled(EventOccurrenceStatus status) {
        Event excluded = event(EventModality.PRESENCIAL);
        occurrence(excluded, FUTURE, valencia, status);

        assertThat(candidates("Valencia")).isEmpty();
        assertThat(candidates(null)).isEmpty();
    }

    @Test
    void includesOnlineEventsWithoutLocationOrInAnotherCity() {
        Event noLocation = eligible(EventModality.ONLINE, null);
        Event otherCity = eligible(EventModality.ONLINE, madrid);

        assertThat(candidates("Valencia")).containsExactly(noLocation.getId(), otherCity.getId());
    }

    @ParameterizedTest
    @EnumSource(value = EventModality.class, names = {"PRESENCIAL", "HIBRIDA"})
    void filtersInPersonAndHybridEventsByCity(EventModality modality) {
        Event local = eligible(modality, valencia);
        eligible(modality, madrid);
        eligible(modality, null);

        assertThat(candidates("Valencia")).containsExactly(local.getId());
    }

    @ParameterizedTest
    @EnumSource(value = EventModality.class, names = {"PRESENCIAL", "HIBRIDA"})
    void requiresTheFutureScheduledOccurrenceToBeInTheRequestedCity(EventModality modality) {
        Event excluded = eligible(modality, madrid);
        occurrence(excluded, NOW.minusSeconds(3600), valencia, EventOccurrenceStatus.PROGRAMADA);
        occurrence(excluded, FUTURE, valencia, EventOccurrenceStatus.CANCELADA);

        assertThat(candidates("Valencia")).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t\n"})
    void includesAllModalitiesAndCitiesWhenCityIsAbsent(String cityName) {
        Event online = eligible(EventModality.ONLINE, null);
        Event inPerson = eligible(EventModality.PRESENCIAL, madrid);
        Event hybrid = eligible(EventModality.HIBRIDA, valencia);

        assertThat(candidates(cityName)).containsExactly(online.getId(), inPerson.getId(), hybrid.getId());
    }

    @Test
    void normalizesCityWhitespaceAndIgnoresCase() {
        Event local = eligible(EventModality.PRESENCIAL, valencia);

        assertThat(candidates("  vAlEnCiA  ")).containsExactly(local.getId());
    }

    @Test
    void limitsResultsToOneHundredUniqueEventsInIdOrder() {
        List<Long> expected = new ArrayList<>();
        for (int i = 0; i < 105; i++) {
            Event event = eligible(EventModality.PRESENCIAL, valencia);
            occurrence(event, FUTURE.plusSeconds(3600), valencia, EventOccurrenceStatus.PROGRAMADA);
            expected.add(event.getId());
        }

        assertThat(candidates("Valencia"))
                .hasSize(100)
                .doesNotHaveDuplicates()
                .containsExactlyElementsOf(expected.subList(0, 100));
    }

    @Test
    void preservesActiveCatalogRulesAndAllowsMissingExperienceType() {
        Event noType = eligible(EventModality.ONLINE, null);
        Event activeType = eligible(EventModality.ONLINE, null);
        activeType.setExperienceType(experienceType("Taller", true));
        Event inactiveType = eligible(EventModality.ONLINE, null);
        inactiveType.setExperienceType(experienceType("Retiro", false));

        Category inactiveCategory = new Category();
        inactiveCategory.setName("Inactiva");
        inactiveCategory.setActive(false);
        entityManager.persist(inactiveCategory);
        Event excluded = eligible(EventModality.ONLINE, null);
        excluded.setCategory(inactiveCategory);

        assertThat(candidates(null)).containsExactly(noType.getId(), activeType.getId());
    }

    @Test
    void returnsAnEmptyListWhenNoEventsAreEligible() {
        assertThat(candidates("Valencia")).isEmpty();
    }

    private List<Long> candidates(String cityName) {
        entityManager.flush();
        entityManager.clear();
        return provider.findCandidates(cityName).stream().map(Event::getId).toList();
    }

    private Event event(EventModality modality) {
        Event event = new Event();
        event.setTitle("Candidate " + ++eventSequence);
        event.setDescription("Event for candidate eligibility tests");
        event.setCategory(category);
        event.setSpecialist(specialist);
        event.setStatus(EventStatus.PUBLICADO);
        event.setModality(modality);
        return entityManager.persist(event);
    }

    private Event eligible(EventModality modality, Location location) {
        Event event = event(modality);
        occurrence(event, FUTURE, location, EventOccurrenceStatus.PROGRAMADA);
        return event;
    }

    private void occurrence(Event event, Instant startsAt, Location location, EventOccurrenceStatus status) {
        EventOccurrence occurrence = new EventOccurrence();
        occurrence.setEvent(event);
        occurrence.setStartsAt(startsAt);
        occurrence.setEndsAt(startsAt.plusSeconds(3600));
        occurrence.setLocation(location);
        occurrence.setStatus(status);
        occurrence.setCapacity(20);
        entityManager.persist(occurrence);
    }

    private Location location(String cityName) {
        City city = new City();
        city.setName(cityName);
        entityManager.persist(city);
        Location location = new Location();
        location.setName("Venue " + cityName);
        location.setAddress("Test address");
        location.setCity(city);
        return entityManager.persist(location);
    }

    private ExperienceType experienceType(String name, boolean active) {
        ExperienceType type = new ExperienceType();
        type.setName(name);
        type.setActive(active);
        return entityManager.persist(type);
    }

    @TestConfiguration
    static class FixedClockConfig {
        @Bean
        Clock fixedClock() {
            return Clock.fixed(NOW, ZoneOffset.UTC);
        }
    }
}
