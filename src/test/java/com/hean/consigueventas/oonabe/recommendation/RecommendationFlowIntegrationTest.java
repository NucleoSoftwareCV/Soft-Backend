package com.hean.consigueventas.oonabe.recommendation;

import com.hean.consigueventas.oonabe.category.entity.Category;
import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.common.enums.EventOccurrenceStatus;
import com.hean.consigueventas.oonabe.common.enums.EventStatus;
import com.hean.consigueventas.oonabe.common.enums.PublicationStatus;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import com.hean.consigueventas.oonabe.experienceType.entity.ExperienceType;
import com.hean.consigueventas.oonabe.interaction.entity.EventFavorite;
import com.hean.consigueventas.oonabe.interaction.entity.ProfessionalFollow;
import com.hean.consigueventas.oonabe.interaction.repository.EventFavoriteRepository;
import com.hean.consigueventas.oonabe.interaction.repository.ProfessionalFollowRepository;
import com.hean.consigueventas.oonabe.masterdata.entity.City;
import com.hean.consigueventas.oonabe.masterdata.entity.Location;
import com.hean.consigueventas.oonabe.profileCliente.entity.ClientEventTypePreference;
import com.hean.consigueventas.oonabe.profileCliente.entity.ClientInterestCategory;
import com.hean.consigueventas.oonabe.profileCliente.entity.ClientModalityPreference;
import com.hean.consigueventas.oonabe.profileCliente.entity.ClientProfile;
import com.hean.consigueventas.oonabe.profileCliente.repository.ClientEventTypePreferenceRepository;
import com.hean.consigueventas.oonabe.profileCliente.repository.ClientInterestCategoryRepository;
import com.hean.consigueventas.oonabe.profileCliente.repository.ClientModalityPreferenceRepository;
import com.hean.consigueventas.oonabe.profileCliente.repository.ClientProfileRepository;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import com.hean.consigueventas.oonabe.recommendation.RecommendationScoringService.ScoredEvent;
import com.hean.consigueventas.oonabe.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
@ActiveProfiles("test")
@Import({RecommendationCandidateProvider.class, RecommendationService.class,
        RecommendationScoringService.class, RecommendationFlowIntegrationTest.FixedClockConfig.class})
class RecommendationFlowIntegrationTest {

    private static final String USERNAME = "recommendation_demo";
    private static final Instant NOW = Instant.parse("2026-10-22T12:00:00Z");

    @Autowired private TestEntityManager entityManager;
    @Autowired private RecommendationService recommendationService;
    @Autowired private RecommendationScoringService scoringService;
    @Autowired private ClientProfileRepository profileRepository;
    @Autowired private ClientInterestCategoryRepository interests;
    @Autowired private ClientEventTypePreferenceRepository types;
    @Autowired private ClientModalityPreferenceRepository modalities;
    @Autowired private ProfessionalFollowRepository follows;
    @Autowired private EventFavoriteRepository favorites;

    private ClientProfile profile;
    private Category yoga;
    private Category otherCategory;
    private ExperienceType workshop;
    private ExperienceType retreat;
    private SpecialistProfile followed;
    private SpecialistProfile otherSpecialist;
    private Location valencia;
    private Location madrid;

    @BeforeEach
    void setUp() {
        valencia = location("Valencia");
        madrid = location("Madrid");
        User client = user(USERNAME);
        profile = new ClientProfile();
        profile.setUser(client);
        profile.setCity(valencia.getCity());
        entityManager.persist(profile);
        yoga = category("Yoga");
        otherCategory = category("Danza");
        workshop = experienceType("Taller");
        retreat = experienceType("Retiro");
        followed = specialist("seguido");
        otherSpecialist = specialist("otro");
    }

    @Test
    @DisplayName("Usuario con preferencias persistidas: 100, 15 y 0 puntos, en orden descendente")
    void scoresPersistedPreferencesAndOrdersEventsWithDifferentMatches() {
        savePreferences();
        Event noMatch = eligible("Danza presencial", otherCategory, retreat, otherSpecialist,
                EventModality.PRESENCIAL, valencia);
        Event partial = eligible("Taller online de danza", otherCategory, workshop, otherSpecialist,
                EventModality.ONLINE, null);
        Event full = eligible("Taller online de yoga", yoga, workshop, followed, EventModality.ONLINE, null);

        List<ScoredEvent> results = recommendations();

        assertThat(results).extracting(result -> result.event().getId())
                .containsExactly(full.getId(), partial.getId(), noMatch.getId());
        assertThat(results).extracting(ScoredEvent::score).containsExactly(
                new RecommendationScore(100, "De un profesional que sigues"),
                new RecommendationScore(15, "Según tus preferencias"),
                new RecommendationScore(0, "Sin coincidencias"));
        System.out.println("Usuario H2: " + USERNAME + "; ciudad Valencia; interés Yoga; tipo Taller; modalidad ONLINE");
        System.out.println("Taller online de yoga: seguido 40 + interés 30 + categoría favorita 15 + tipo 10 + modalidad 5 = 100");
        System.out.println("Taller online de danza: tipo 10 + modalidad 5 = 15");
        System.out.println("Danza presencial: ninguna coincidencia = 0");
    }

    @Test
    @DisplayName("Evento elegible sin coincidencias: se conserva con 0 puntos")
    void keepsAnEligibleEventWithNoMatches() {
        savePreferences();
        Event event = eligible("Sin coincidencias", otherCategory, retreat, otherSpecialist,
                EventModality.PRESENCIAL, valencia);

        List<ScoredEvent> results = recommendations();

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().event().getId()).isEqualTo(event.getId());
        assertThat(results.getFirst().score()).isEqualTo(new RecommendationScore(0, "Sin coincidencias"));
    }

    @Test
    @DisplayName("Usuario sin preferencias: 0 puntos y orden original de los candidatos")
    void returnsZeroAndPreservesCandidateOrderWithoutPreferences() {
        profile.setCity(null);
        Event first = eligible("Primero", yoga, workshop, followed, EventModality.ONLINE, null);
        Event second = eligible("Segundo", otherCategory, retreat, otherSpecialist,
                EventModality.PRESENCIAL, madrid);

        List<ScoredEvent> results = recommendations();

        assertThat(results).extracting(result -> result.event().getId()).containsExactly(first.getId(), second.getId());
        assertThat(results).extracting(ScoredEvent::score).containsOnly(new RecommendationScore(0, "Sin coincidencias"));
    }

    @Test
    @DisplayName("Sin eventos elegibles: lista vacía incluso con preferencias registradas")
    void returnsEmptyWhenNoCandidatesExist() {
        savePreferences();

        assertThat(recommendations()).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = EventStatus.class, names = "PUBLICADO", mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("Un evento no publicado se descarta aunque coincida con todas las preferencias")
    void excludesUnpublishedEventsBeforeScoring(EventStatus status) {
        savePreferences();
        Event excluded = eligible("No publicado", yoga, workshop, followed, EventModality.ONLINE, null);
        excluded.setStatus(status);

        assertThat(recommendations()).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = EventOccurrenceStatus.class, names = "PROGRAMADA", mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("Una ocurrencia futura no programada no hace elegible al evento")
    void excludesInvalidFutureOccurrencesBeforeScoring(EventOccurrenceStatus status) {
        savePreferences();
        Event excluded = event("Fecha inválida", yoga, workshop, followed, EventModality.ONLINE);
        occurrence(excluded, NOW.plusSeconds(3600), null, status);

        assertThat(recommendations()).isEmpty();
    }

    @Test
    @DisplayName("Sin ocurrencias o con fechas pasadas: descartados antes del puntaje")
    void excludesEventsWithoutFutureOccurrences() {
        savePreferences();
        event("Sin fechas", yoga, workshop, followed, EventModality.ONLINE);
        Event past = event("Fecha pasada", yoga, workshop, followed, EventModality.ONLINE);
        occurrence(past, NOW.minusSeconds(1), null, EventOccurrenceStatus.PROGRAMADA);

        assertThat(recommendations()).isEmpty();
    }

    @Test
    @DisplayName("Ciudad: presenciales e híbridos locales más online de cualquier ciudad o sin ubicación")
    void filtersByCityAndIncludesOnlineEvents() {
        savePreferences();
        Event local = eligible("Local", yoga, workshop, followed, EventModality.PRESENCIAL, valencia);
        Event hybrid = eligible("Híbrido local", yoga, workshop, followed, EventModality.HIBRIDA, valencia);
        Event onlineElsewhere = eligible("Online otra ciudad", yoga, workshop, followed, EventModality.ONLINE, madrid);
        Event onlineNoLocation = eligible("Online sin ubicación", yoga, workshop, followed, EventModality.ONLINE, null);
        eligible("Presencial otra ciudad", yoga, workshop, followed, EventModality.PRESENCIAL, madrid);
        eligible("Híbrido otra ciudad", yoga, workshop, followed, EventModality.HIBRIDA, madrid);
        Event split = eligible("Pasado local y futuro fuera", yoga, workshop, followed, EventModality.PRESENCIAL, madrid);
        occurrence(split, NOW.minusSeconds(3600), valencia, EventOccurrenceStatus.PROGRAMADA);

        List<ScoredEvent> results = recommendations();

        assertThat(results).extracting(result -> result.event().getId()).containsExactly(
                onlineElsewhere.getId(), onlineNoLocation.getId(), local.getId(), hybrid.getId());
        assertThat(results).extracting(result -> result.score().score()).containsExactly(100, 100, 95, 95);
    }

    // Test-only orchestration: preference loading is not yet wired into the production service.
    private List<ScoredEvent> recommendations() {
        Long userId = profile.getUser().getId();
        entityManager.flush();
        entityManager.clear();
        ClientProfile stored = profileRepository.findByUserId(userId).orElseThrow();
        Long profileId = stored.getId();
        RecommendationSignals signals = new RecommendationSignals(
                interests.findByClientProfileId(profileId).stream().map(row -> row.getCategory().getId()).collect(Collectors.toSet()),
                favorites.findByClientProfileIdOrderByCreatedAtDesc(profileId).stream()
                        .map(row -> row.getEvent().getCategory().getId()).collect(Collectors.toSet()),
                types.findByClientProfileId(profileId).stream().map(row -> row.getExperienceType().getId()).collect(Collectors.toSet()),
                follows.findByClientProfileId(profileId, Pageable.unpaged()).stream()
                        .map(row -> row.getSpecialistProfile().getId()).collect(Collectors.toSet()),
                modalities.findByClientProfileId(profileId).map(ClientModalityPreference::getModality).orElse(null),
                stored.getCity() == null ? null : stored.getCity().getName());
        return scoringService.scoreAndSort(recommendationService.getRecommendations(USERNAME), signals);
    }

    private void savePreferences() {
        ClientInterestCategory interest = new ClientInterestCategory();
        interest.setClientProfile(profile);
        interest.setCategory(yoga);
        entityManager.persist(interest);
        ClientEventTypePreference type = new ClientEventTypePreference();
        type.setClientProfile(profile);
        type.setExperienceType(workshop);
        entityManager.persist(type);
        ClientModalityPreference modality = new ClientModalityPreference();
        modality.setClientProfile(profile);
        modality.setModality(EventModality.ONLINE);
        entityManager.persist(modality);
        ProfessionalFollow follow = new ProfessionalFollow();
        follow.setClientProfile(profile);
        follow.setSpecialistProfile(followed);
        entityManager.persist(follow);
        Event historical = event("Favorito anterior", yoga, retreat, otherSpecialist, EventModality.PRESENCIAL);
        historical.setStatus(EventStatus.ARCHIVADO);
        EventFavorite favorite = new EventFavorite();
        favorite.setClientProfile(profile);
        favorite.setEvent(historical);
        entityManager.persist(favorite);
    }

    private User user(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        return entityManager.persist(user);
    }

    private SpecialistProfile specialist(String slug) {
        SpecialistProfile specialist = new SpecialistProfile();
        specialist.setUser(user(slug));
        specialist.setSlug(slug);
        specialist.setPublicName(slug);
        specialist.setWhatsappPhone("+34600000000");
        specialist.setPublicationStatus(PublicationStatus.PUBLICADO);
        return entityManager.persist(specialist);
    }

    private Category category(String name) {
        Category category = new Category();
        category.setName(name);
        return entityManager.persist(category);
    }

    private ExperienceType experienceType(String name) {
        ExperienceType type = new ExperienceType();
        type.setName(name);
        return entityManager.persist(type);
    }

    private Location location(String name) {
        City city = new City();
        city.setName(name);
        entityManager.persist(city);
        Location location = new Location();
        location.setCity(city);
        location.setName("Local " + name);
        location.setAddress("Dirección de prueba");
        return entityManager.persist(location);
    }

    private Event event(String title, Category category, ExperienceType type, SpecialistProfile specialist, EventModality modality) {
        Event event = new Event();
        event.setTitle(title);
        event.setDescription("Evento de integración");
        event.setCategory(category);
        event.setExperienceType(type);
        event.setSpecialist(specialist);
        event.setModality(modality);
        event.setStatus(EventStatus.PUBLICADO);
        return entityManager.persist(event);
    }

    private Event eligible(String title, Category category, ExperienceType type, SpecialistProfile specialist,
                           EventModality modality, Location location) {
        Event event = event(title, category, type, specialist, modality);
        occurrence(event, NOW.plusSeconds(3600), location, EventOccurrenceStatus.PROGRAMADA);
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

    @TestConfiguration
    static class FixedClockConfig {
        @Bean
        Clock fixedClock() {
            return Clock.fixed(NOW, ZoneOffset.UTC);
        }
    }
}
