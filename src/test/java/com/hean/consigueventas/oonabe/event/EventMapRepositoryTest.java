package com.hean.consigueventas.oonabe.event;

import com.hean.consigueventas.oonabe.category.entity.Category;
import com.hean.consigueventas.oonabe.common.enums.*;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import com.hean.consigueventas.oonabe.event.repository.EventMapRepository;
import com.hean.consigueventas.oonabe.masterdata.entity.City;
import com.hean.consigueventas.oonabe.masterdata.entity.Location;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import com.hean.consigueventas.oonabe.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:event_map;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EventMapRepositoryTest {
    private static final Instant NOW = Instant.parse("2026-10-22T12:00:00Z");
    @Autowired TestEntityManager em;
    @Autowired EventMapRepository repository;
    private User owner;
    private SpecialistProfile specialist;
    private Category category;
    private Event event;
    private Location location;
    private EventOccurrence occurrence;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setUsername("map-owner");
        owner.setEmail("map-owner@example.com");
        owner.setStatus(UserStatus.ACTIVO);
        em.persist(owner);

        specialist = new SpecialistProfile();
        specialist.setUser(owner);
        specialist.setSlug("map-owner");
        specialist.setPublicName("Profesional mapa");
        specialist.setWhatsappPhone("+34600000000");
        specialist.setApprovalStatus(ApprovalStatus.APROBADO);
        specialist.setPublicationStatus(PublicationStatus.PUBLICADO);
        em.persist(specialist);

        category = new Category();
        category.setName("Yoga mapa");
        category.setActive(true);
        em.persist(category);

        City city = new City();
        city.setName("Valencia");
        city.setIsActive(true);
        em.persist(city);

        location = new Location();
        location.setName("Centro");
        location.setAddress("Calle de prueba 12");
        location.setCity(city);
        location.setIsActive(true);
        location.setLatitude(new BigDecimal("39.469900"));
        location.setLongitude(new BigDecimal("-0.376300"));
        em.persist(location);

        event = new Event();
        event.setTitle("Yoga en Valencia");
        event.setDescription("Evento público de prueba");
        event.setCategory(category);
        event.setSpecialist(specialist);
        event.setModality(EventModality.PRESENCIAL);
        event.setStatus(EventStatus.PUBLICADO);
        em.persist(event);

        occurrence = createOccurrence(NOW.plusSeconds(3600));
    }


    @Test
    void travelDestinationsExcludeOnlinePrivateAndInvalidLocations() {
        var ids = java.util.List.of(occurrence.getId(), Long.MAX_VALUE);
        em.flush();
        assertThat(repository.findTravelDestinations(NOW, ids)).hasSize(1);
        event.setModality(EventModality.ONLINE); em.flush();
        assertThat(repository.findTravelDestinations(NOW, ids)).isEmpty();
        event.setModality(EventModality.PRESENCIAL); event.setStatus(EventStatus.BORRADOR); em.flush();
        assertThat(repository.findTravelDestinations(NOW, ids)).isEmpty();
        event.setStatus(EventStatus.PUBLICADO); location.setLatitude(null); em.flush();
        assertThat(repository.findTravelDestinations(NOW, ids)).isEmpty();
    }

    @Test
    void returnsCoordinatesAndAddressWithoutLoadingEachEventDetail() {
        event.setMood(EventMood.CONECTAR);
        em.flush();
        em.clear();
        var page = repository.findAvailableLocations(NOW, PageRequest.of(0, 100));
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().getFirst().categoryId()).isEqualTo(category.getId());
        assertThat(page.getContent().getFirst().mood()).isEqualTo(EventMood.CONECTAR);
        assertThat(page.getContent().getFirst().organizerName()).isEqualTo("Profesional mapa");
        assertThat(page.getContent().getFirst().modality()).isEqualTo(EventModality.PRESENCIAL);
        assertThat(page.getContent().getFirst().address()).isEqualTo("Calle de prueba 12");
        assertThat(page.getContent().getFirst().latitude()).isEqualByComparingTo("39.469900");
    }

    @Test
    void excludesOnlineAndIncludesHybridEventsWithPhysicalLocation() {
        event.setModality(EventModality.ONLINE);
        assertEmpty();
        event.setModality(EventModality.HIBRIDA);
        em.flush();
        assertThat(repository.findAvailableLocations(NOW, PageRequest.of(0, 100)).getTotalElements()).isEqualTo(1);
    }

    @Test
    void excludesMissingAndInvalidCoordinatesButAcceptsZero() {
        location.setLatitude(null);
        assertEmpty();
        location.setLatitude(new BigDecimal("91"));
        assertEmpty();
        location.setLatitude(BigDecimal.ZERO);
        location.setLongitude(new BigDecimal("-181"));
        assertEmpty();
        location.setLongitude(BigDecimal.ZERO);
        em.flush();
        assertThat(repository.findAvailableLocations(NOW, PageRequest.of(0, 100)).getTotalElements()).isEqualTo(1);
    }

    @Test
    void excludesPastCancelledAndSoldOutSessions() {
        occurrence.setStartsAt(NOW);
        assertEmpty();
        occurrence.setStartsAt(NOW.plusSeconds(3600));
        occurrence.setStatus(EventOccurrenceStatus.CANCELADA);
        assertEmpty();
        occurrence.setStatus(EventOccurrenceStatus.PROGRAMADA);
        occurrence.setReservedSpots(10);
        assertEmpty();
    }

    @Test
    void checksPublicationAndOrganizerVisibility() {
        event.setStatus(EventStatus.BORRADOR);
        assertEmpty();
        event.setStatus(EventStatus.PUBLICADO);
        specialist.setPublicationStatus(PublicationStatus.OCULTO);
        assertEmpty();
        specialist.setPublicationStatus(PublicationStatus.PUBLICADO);
        owner.setStatus(UserStatus.SUSPENDIDO);
        assertEmpty();
        owner.setStatus(UserStatus.ACTIVO);
        category.setActive(false);
        assertEmpty();
    }

    @Test
    void excludesInactiveLocationsAndCities() {
        location.setIsActive(false);
        assertEmpty();
        location.setIsActive(true);
        location.getCity().setIsActive(false);
        assertEmpty();
    }

    @Test
    void pagesAllFutureSessionsWithStableOrderEvenAtSameVenue() {
        EventOccurrence second = createOccurrence(occurrence.getStartsAt());
        createOccurrence(NOW.plusSeconds(7200));
        em.flush();
        var firstPage = repository.findAvailableLocations(NOW, PageRequest.of(0, 1));
        var secondPage = repository.findAvailableLocations(NOW, PageRequest.of(1, 1));
        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getContent().getFirst().occurrenceId()).isEqualTo(occurrence.getId());
        assertThat(secondPage.getContent().getFirst().occurrenceId()).isEqualTo(second.getId());
    }

    private void assertEmpty() {
        em.flush();
        assertThat(repository.findAvailableLocations(NOW, PageRequest.of(0, 100))).isEmpty();
    }

    private EventOccurrence createOccurrence(Instant startsAt) {
        EventOccurrence result = new EventOccurrence();
        result.setEvent(event);
        result.setLocation(location);
        result.setStartsAt(startsAt);
        result.setEndsAt(startsAt.plusSeconds(3600));
        result.setCapacity(10);
        result.setReservedSpots(0);
        result.setStatus(EventOccurrenceStatus.PROGRAMADA);
        return em.persist(result);
    }
}
