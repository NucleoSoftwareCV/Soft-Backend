package com.hean.consigueventas.oonabe.recommendation;

import com.hean.consigueventas.oonabe.category.entity.Category;
import com.hean.consigueventas.oonabe.common.enums.ApprovalStatus;
import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.common.enums.EventOccurrenceStatus;
import com.hean.consigueventas.oonabe.common.enums.EventStatus;
import com.hean.consigueventas.oonabe.common.enums.PublicationStatus;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import com.hean.consigueventas.oonabe.event.repository.EventOccurrenceRepository;
import com.hean.consigueventas.oonabe.experienceType.entity.ExperienceType;
import com.hean.consigueventas.oonabe.masterdata.entity.City;
import com.hean.consigueventas.oonabe.masterdata.entity.Location;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import com.hean.consigueventas.oonabe.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:event_recommendations;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class EventRecommendationRepositoryTest {

    private static final Instant NOW =
            Instant.parse("2026-10-16T12:00:00Z");

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EventOccurrenceRepository repository;

    private User owner;
    private SpecialistProfile specialist;
    private Category category;
    private Event event;
    private EventOccurrence occurrence;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setUsername("teacher");
        owner.setEmail("teacher@example.com");
        entityManager.persist(owner);

        specialist = new SpecialistProfile();
        specialist.setUser(owner);
        specialist.setSlug("teacher");
        specialist.setPublicName("Profesional de prueba");
        specialist.setWhatsappPhone("+51999111222");
        specialist.setApprovalStatus(ApprovalStatus.APROBADO);
        specialist.setPublicationStatus(PublicationStatus.PUBLICADO);
        entityManager.persist(specialist);

        category = new Category();
        category.setName("Yoga");
        entityManager.persist(category);

        event = new Event();
        event.setTitle("Evento de prueba");
        event.setDescription("Actividad para validar recomendaciones");
        event.setCategory(category);
        event.setSpecialist(specialist);
        event.setModality(EventModality.ONLINE);
        event.setStatus(EventStatus.PUBLICADO);
        entityManager.persist(event);

        occurrence = createOccurrence(NOW.plusSeconds(3600), null);
    }

    @Test
    void shouldIncludeOnlineEventRegardlessOfCity() {
        assertThat(query("Lima"))
                .extracting(EventOccurrence::getId)
                .containsExactly(occurrence.getId());

        assertThat(query("Cusco"))
                .extracting(EventOccurrence::getId)
                .containsExactly(occurrence.getId());
    }

    @Test
    void shouldExcludeNonPublicEventsAndUnavailableProfessionals() {
        assertThat(query(null)).hasSize(1);

        event.setStatus(EventStatus.BORRADOR);
        assertThat(query(null)).isEmpty();
        event.setStatus(EventStatus.PUBLICADO);

        specialist.setPublicationStatus(PublicationStatus.BORRADOR);
        assertThat(query(null)).isEmpty();
        specialist.setPublicationStatus(PublicationStatus.PUBLICADO);

        specialist.setApprovalStatus(ApprovalStatus.PENDIENTE);
        assertThat(query(null)).isEmpty();
        specialist.setApprovalStatus(ApprovalStatus.APROBADO);

        owner.setActive(false);
        assertThat(query(null)).isEmpty();

        owner.setActive(true);
        assertThat(query(null)).hasSize(1);
    }

    @Test
    void shouldExcludeInactiveCategoriesAndExperienceTypes() {
        assertThat(query(null)).hasSize(1);

        category.setActive(false);
        assertThat(query(null)).isEmpty();
        category.setActive(true);

        ExperienceType type = new ExperienceType();
        type.setName("Taller");
        type.setActive(false);
        entityManager.persist(type);

        event.setExperienceType(type);
        assertThat(query(null)).isEmpty();

        type.setActive(true);
        assertThat(query(null)).hasSize(1);
    }

    @Test
    void shouldRequireFutureProgrammedOccurrenceWithAvailableSpots() {
        assertThat(query(null)).hasSize(1);

        occurrence.setStartsAt(NOW.minusSeconds(60));
        assertThat(query(null)).isEmpty();

        occurrence.setStartsAt(NOW);
        assertThat(query(null)).isEmpty();

        occurrence.setStartsAt(NOW.plusSeconds(3600));
        occurrence.setStatus(EventOccurrenceStatus.CANCELADA);
        assertThat(query(null)).isEmpty();

        occurrence.setStatus(EventOccurrenceStatus.PROGRAMADA);
        occurrence.setReservedSpots(10);
        assertThat(query(null)).isEmpty();

        occurrence.setReservedSpots(9);
        assertThat(query(null)).hasSize(1);
    }

    @ParameterizedTest
    @EnumSource(
            value = EventModality.class,
            names = {"PRESENCIAL", "HIBRIDA"}
    )
    void shouldFilterLocalEventsByCity(EventModality modality) {
        event.setModality(modality);
        occurrence.setLocation(createLocation("Lima"));

        assertThat(query("lima"))
                .extracting(EventOccurrence::getId)
                .containsExactly(occurrence.getId());

        assertThat(query("Cusco")).isEmpty();
    }

    @Test
    void shouldRequireActiveLocationAndCityForLocalEvents() {
        event.setModality(EventModality.PRESENCIAL);

        assertThat(query("Lima")).isEmpty();

        Location location = createLocation("Lima");
        occurrence.setLocation(location);
        assertThat(query("Lima")).hasSize(1);

        location.setIsActive(false);
        assertThat(query("Lima")).isEmpty();

        location.setIsActive(true);
        location.getCity().setIsActive(false);
        assertThat(query("Lima")).isEmpty();

        location.getCity().setIsActive(true);
        assertThat(query("Lima")).hasSize(1);
    }

    @Test
    void shouldCheckCityAndCapacityOnTheSameOccurrence() {
        event.setModality(EventModality.PRESENCIAL);

        occurrence.setLocation(createLocation("Lima"));
        occurrence.setReservedSpots(10);

        EventOccurrence availableInCusco = createOccurrence(
                NOW.plusSeconds(7200),
                createLocation("Cusco")
        );

        assertThat(query("Lima")).isEmpty();

        assertThat(query("Cusco"))
                .extracting(EventOccurrence::getId)
                .containsExactly(availableInCusco.getId());
    }

    @Test
    void shouldAllowAnyActiveCityWhenNoCityIsSelected() {
        event.setModality(EventModality.PRESENCIAL);
        occurrence.setLocation(createLocation("Cusco"));

        assertThat(query(null))
                .extracting(EventOccurrence::getId)
                .containsExactly(occurrence.getId());
    }

    private List<EventOccurrence> query(String cityName) {
        entityManager.flush();

        return repository.findRecommendationCandidates(NOW, cityName);
    }

    private Location createLocation(String cityName) {
        City city = new City();
        city.setName(cityName);
        city.setIsActive(true);
        entityManager.persist(city);

        Location location = new Location();
        location.setName("Local " + cityName);
        location.setAddress("Direccion de prueba");
        location.setCity(city);
        location.setIsActive(true);

        return entityManager.persist(location);
    }

    private EventOccurrence createOccurrence(
            Instant startsAt,
            Location location
    ) {
        EventOccurrence result = new EventOccurrence();
        result.setEvent(event);
        result.setStartsAt(startsAt);
        result.setEndsAt(startsAt.plusSeconds(3600));
        result.setCapacity(10);
        result.setReservedSpots(0);
        result.setStatus(EventOccurrenceStatus.PROGRAMADA);
        result.setLocation(location);

        return entityManager.persist(result);
    }
}