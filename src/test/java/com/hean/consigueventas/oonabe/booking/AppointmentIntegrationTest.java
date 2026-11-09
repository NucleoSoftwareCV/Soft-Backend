package com.hean.consigueventas.oonabe.booking;

import com.hean.consigueventas.oonabe.appointment.*;
import static com.hean.consigueventas.oonabe.appointment.AppointmentDtos.*;
import com.hean.consigueventas.oonabe.auth.security.UserDetailsImpl;
import com.hean.consigueventas.oonabe.booking.entity.SessionBooking;
import com.hean.consigueventas.oonabe.booking.exception.BookingConflictException;
import com.hean.consigueventas.oonabe.common.enums.*;
import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import com.hean.consigueventas.oonabe.masterdata.entity.Location;
import com.hean.consigueventas.oonabe.oneToOneSession.entity.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
@Import(AppointmentIntegrationTest.FixedTime.class)
class AppointmentIntegrationTest {
    @TestConfiguration static class FixedTime {
        @Bean @Primary Clock appointmentClock() { return Clock.fixed(Instant.parse("2026-11-05T12:00:00Z"),ZoneId.of("America/Lima")); }
    }
    @Autowired AppointmentService appointments;
    @Autowired EntityManager em;
    @Autowired MockMvc mvc;
    BookingFixtures.Fixture fixture;
    OneToOneService service;
    Location location;
    final LocalDate day=LocalDate.of(2026,11,9);
    final Instant start=Instant.parse("2026-11-09T14:00:00Z");
    @BeforeEach void setup() {
        fixture=BookingFixtures.create(em,UUID.randomUUID().toString().substring(0,8));
        location=new Location(); location.setName("Sede de prueba"); location.setAddress("Lima, Perú"); location.setIsActive(true); em.persist(location);
        service=new OneToOneService(); service.setSpecialist(fixture.occurrence().getEvent().getSpecialist());
        service.setSlug("cita-"+UUID.randomUUID()); service.setTitle("Consulta individual"); service.setDescription("Servicio para verificar citas");
        service.setDurationMinutes(60); service.setModality(SessionModality.AMBAS); service.setPrice(new BigDecimal("50.00")); service.setCurrency("PEN"); service.setStatus(PublicationStatus.PUBLICADO);
        em.persist(service); em.flush();
        appointments.addRule(fixture.professional().getId(),rule(null));
        appointments.addRule(fixture.professional().getId(),rule(location.getId()));
    }
    RuleInput rule(Long locationId) { return new RuleInput(1,LocalTime.of(9,0),LocalTime.of(12,0),day,day.plusDays(30),locationId); }
    Request input() { return new Request(service.getId(),SessionModality.ONLINE,null,start,"Motivo de prueba"); }
    Receipt request() { return appointments.request(fixture.client().getId(),input()); }
    UserDetailsImpl principal(Long id,String role) { return new UserDetailsImpl(id,"prueba","prueba@example.test","",true,List.of(new SimpleGrantedAuthority("ROLE_"+role))); }
    @Test void optionsExposeActualOpeningDaysForTheService() {
        var options=appointments.options(service.getId());
        assertThat(options.openings()).hasSize(2);
        assertThat(options.openings()).extracting(Opening::dayOfWeek).containsOnly(1);
        assertThat(options.openings()).extracting(Opening::validFrom).containsOnly(day);
        service.setDurationMinutes(240);
        assertThat(appointments.options(service.getId()).openings()).isEmpty();
    }
    @Test void missingConfigurationIsReportedAsEmptyOpenings() {
        for(var rule:appointments.rules(fixture.professional().getId())) appointments.removeRule(fixture.professional().getId(),rule.id());
        assertThat(appointments.options(service.getId()).openings()).isEmpty();
    }
    @Test void offersOnlyConfiguredHoursInPeru() {
        var slots=appointments.availability(service.getId(),day,SessionModality.ONLINE,null);
        assertThat(slots.timeZone()).isEqualTo("America/Lima");
        assertThat(slots.slots()).extracting(Slot::startsAt).containsExactly(start,start.plusSeconds(3600),start.plusSeconds(7200));
        assertThat(appointments.availability(service.getId(),day.plusDays(1),SessionModality.ONLINE,null).slots()).isEmpty();
    }
    @Test void physicalSlotsRequireAnActiveVenueAndRespectServiceVenue() {
        assertThat(appointments.options(service.getId()).venues()).hasSize(1);
        assertThat(appointments.availability(service.getId(),day,SessionModality.PRESENCIAL,location.getId()).slots()).hasSize(3);
        assertThatThrownBy(()->appointments.availability(service.getId(),day,SessionModality.PRESENCIAL,null)).isInstanceOf(IllegalArgumentException.class);
        location.setIsActive(false);
        assertThatThrownBy(()->appointments.availability(service.getId(),day,SessionModality.PRESENCIAL,location.getId())).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void storesAuthenticatedCustomerAndServerCalculatedPriceEndAndStatus() {
        var receipt=request(); em.flush(); em.clear();
        var saved=em.find(SessionBooking.class,receipt.id());
        assertThat(saved.getCustomer().getUser().getId()).isEqualTo(fixture.client().getId());
        assertThat(saved.getPrice()).isEqualByComparingTo("50.00"); assertThat(saved.getCurrency()).isEqualTo("PEN");
        assertThat(saved.getEndsAt()).isEqualTo(start.plusSeconds(3600)); assertThat(saved.getStatus()).isEqualTo(BookingStatus.PENDIENTE);
        assertThat(saved.getExpiresAt()).isEqualTo(Instant.parse("2026-11-06T12:00:00Z"));
        assertThat(saved.getOrderItem()).isNull();
    }
    @Test void blocksDuplicateAndCrossModalityBookings() {
        request();
        assertThatThrownBy(this::request).isInstanceOf(BookingConflictException.class);
        assertThat(appointments.availability(service.getId(),day,SessionModality.PRESENCIAL,location.getId()).slots()).extracting(Slot::startsAt).doesNotContain(start);
    }
    @Test void respectsBlocksAndSessionDurationWithoutRoundingArbitraryStarts() {
        var block=new ScheduleBlock(); block.setSpecialist(service.getSpecialist()); block.setStartsAt(start.plusSeconds(1800)); block.setEndsAt(start.plusSeconds(5400)); em.persist(block); em.flush();
        assertThat(appointments.availability(service.getId(),day,SessionModality.ONLINE,null).slots()).extracting(Slot::startsAt).containsExactly(start.plusSeconds(7200));
        assertThatThrownBy(()->appointments.request(fixture.client().getId(),new Request(service.getId(),SessionModality.ONLINE,null,start.plusSeconds(60),"Prueba"))).isInstanceOf(BookingConflictException.class);
    }
    @Test void listsOnlyOwnActivePendingRequestsAndIncludesExpiredOnesInTheCalendar() {
        var receipt = request();
        var owner = fixture.professional().getId();
        var other = BookingFixtures.create(em, "agenda" + UUID.randomUUID().toString().substring(0, 5));

        assertThat(appointments.pending(owner)).extracting(Receipt::id).containsExactly(receipt.id());
        assertThat(appointments.pending(other.professional().getId())).isEmpty();
        assertThat(appointments.agendaRange(owner, day.minusDays(1), day))
                .extracting(Receipt::id).containsExactly(receipt.id());
        assertThat(appointments.agendaRange(owner, day.plusDays(1), day.plusDays(2))).isEmpty();
        assertThatThrownBy(() -> appointments.agendaRange(owner, day, day.plusDays(31)))
                .isInstanceOf(IllegalArgumentException.class);

        em.find(SessionBooking.class, receipt.id()).setExpiresAt(Instant.parse("2026-11-05T11:00:00Z"));
        em.flush();
        assertThat(appointments.pending(owner)).isEmpty();
        assertThat(appointments.agendaRange(owner, day, day)).extracting(Receipt::status).containsExactly("EXPIRADA");
    }

    @Test void expiredPendingRequestsReleaseTheSlot() {
        var receipt=request(); var b=em.find(SessionBooking.class,receipt.id()); b.setExpiresAt(Instant.parse("2026-11-05T11:00:00Z")); em.flush();
        assertThat(appointments.availability(service.getId(),day,SessionModality.ONLINE,null).slots()).extracting(Slot::startsAt).contains(start);
        assertThat(appointments.agenda(fixture.professional().getId(),day).getFirst().status()).isEqualTo("EXPIRADA");
        assertThatThrownBy(()->appointments.decide(fixture.professional().getId(),receipt.id(),BookingStatus.CONFIRMADA,null)).isInstanceOf(BookingConflictException.class);
    }
    @Test void confirmedBookingsRemainBusyAndCancellationReleasesSlot() {
        var receipt=request();
        assertThat(appointments.decide(fixture.professional().getId(),receipt.id(),BookingStatus.CONFIRMADA,null).status()).isEqualTo("CONFIRMADA");
        assertThat(appointments.availability(service.getId(),day,SessionModality.ONLINE,null).slots()).hasSize(2);
        var cancelled = appointments.decide(fixture.professional().getId(),receipt.id(),BookingStatus.CANCELADA,"Cambio de agenda");
        assertThat(cancelled.cancellationReason()).isEqualTo("Cambio de agenda");
        assertThat(cancelled.cancelledBy()).isEqualTo("PROFESIONAL");
        assertThat(appointments.availability(service.getId(),day,SessionModality.ONLINE,null).slots()).hasSize(3);
    }
    @Test void customerCanCancelTheirConfirmedAppointmentAndReleaseTheSlot() throws Exception {
        var receipt = request();
        var owner = fixture.client().getId();
        var other = BookingFixtures.create(em, "cancel" + UUID.randomUUID().toString().substring(0, 5));
        appointments.decide(fixture.professional().getId(), receipt.id(), BookingStatus.CONFIRMADA, null);
        String path = "/api/v1/appointments/requests/" + receipt.id() + "/cancel";

        String cancellation = "{\"reason\":\"Cambio de planes\"}";
        mvc.perform(patch(path).with(user(principal(other.client().getId(), "USER")))
                        .contentType("application/json").content(cancellation))
                .andExpect(status().isNotFound());
        mvc.perform(patch(path).with(user(principal(owner, "USER")))
                        .contentType("application/json").content(cancellation))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELADA"))
                .andExpect(jsonPath("$.cancellationReason").value("Cambio de planes"))
                .andExpect(jsonPath("$.cancelledBy").value("CLIENTE"));
        assertThat(appointments.agenda(fixture.professional().getId(), day))
                .extracting(Receipt::status).containsExactly("CANCELADA");
        assertThat(appointments.availability(service.getId(), day, SessionModality.ONLINE, null).slots())
                .extracting(Slot::startsAt).contains(start);
        mvc.perform(patch(path).with(user(principal(owner, "USER")))
                        .contentType("application/json").content(cancellation))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELADA"));
    }

    @Test void customerCanRescheduleAndProfessionalMustAcceptTheNewTime() throws Exception {
        var receipt = request();
        appointments.decide(fixture.professional().getId(), receipt.id(), BookingStatus.CONFIRMADA, null);
        var newStart = start.plusSeconds(3600);
        String path = "/api/v1/appointments/requests/" + receipt.id() + "/reschedule";
        String body = "{\"modality\":\"ONLINE\",\"startsAt\":\"" + newStart
                + "\",\"reason\":\"Necesito asistir una hora después\"}";

        mvc.perform(patch(path).with(user(principal(fixture.client().getId(), "USER")))
                        .contentType("application/json").content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDIENTE"))
                .andExpect(jsonPath("$.startsAt").value(newStart.toString()))
                .andExpect(jsonPath("$.previousStartsAt").value(start.toString()))
                .andExpect(jsonPath("$.rescheduleReason").value("Necesito asistir una hora después"));

        var updated = em.find(SessionBooking.class, receipt.id());
        assertThat(updated.getPreviousEndsAt()).isEqualTo(start.plusSeconds(3600));
        assertThat(updated.getExpiresAt()).isEqualTo(Instant.parse("2026-11-06T12:00:00Z"));
        assertThat(appointments.availability(service.getId(), day, SessionModality.ONLINE, null).slots())
                .extracting(Slot::startsAt).contains(start).doesNotContain(newStart);
        assertThat(appointments.pending(fixture.professional().getId())).extracting(Receipt::id)
                .containsExactly(receipt.id());
    }

    @Test void rescheduleRejectsAnotherCustomerAndAnOccupiedSlot() throws Exception {
        var receipt = request();
        var other = BookingFixtures.create(em, "move" + UUID.randomUUID().toString().substring(0, 5));
        String path = "/api/v1/appointments/requests/" + receipt.id() + "/reschedule";
        String body = "{\"modality\":\"ONLINE\",\"startsAt\":\"" + start.plusSeconds(3600)
                + "\",\"reason\":\"Cambio de horario\"}";

        mvc.perform(patch(path).with(user(principal(other.client().getId(), "USER")))
                        .contentType("application/json").content(body))
                .andExpect(status().isNotFound());

        appointments.request(other.client().getId(),
                new Request(service.getId(), SessionModality.ONLINE, null, start.plusSeconds(3600), "Otra cita"));
        mvc.perform(patch(path).with(user(principal(fixture.client().getId(), "USER")))
                        .contentType("application/json").content(body))
                .andExpect(status().isConflict());
    }

    @Test void doesNotCompleteAFutureAppointment() {
        var receipt=request(); appointments.decide(fixture.professional().getId(),receipt.id(),BookingStatus.CONFIRMADA,null);
        assertThatThrownBy(()->appointments.decide(fixture.professional().getId(),receipt.id(),BookingStatus.COMPLETADA,null)).isInstanceOf(BookingConflictException.class);
    }
    @Test void anotherProfessionalCannotManageTheRequestOrRules() {
        var receipt=request(); var other=BookingFixtures.create(em,"otro"+UUID.randomUUID().toString().substring(0,5));
        assertThatThrownBy(()->appointments.decide(other.professional().getId(),receipt.id(),BookingStatus.CANCELADA,"No disponible")).isInstanceOf(ResourceNotFoundException.class);
        var rule=appointments.rules(fixture.professional().getId()).getFirst();
        assertThatThrownBy(()->appointments.removeRule(other.professional().getId(),rule.id())).isInstanceOf(ResourceNotFoundException.class);
    }
    @Test void rejectsInvalidDatesAndUnsupportedModes() {
        assertThatThrownBy(()->appointments.availability(service.getId(),day.minusDays(10),SessionModality.ONLINE,null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->appointments.availability(service.getId(),day.plusDays(100),SessionModality.ONLINE,null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->appointments.availability(service.getId(),day,SessionModality.AMBAS,null)).isInstanceOf(IllegalArgumentException.class);
        service.setModality(SessionModality.PRESENCIAL);
        assertThatThrownBy(()->appointments.availability(service.getId(),day,SessionModality.ONLINE,null)).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void rejectsSelfBookingEvenIfProfessionalAlsoHasACustomerProfile() {
        var customer=new com.hean.consigueventas.oonabe.profileCliente.entity.CustomerProfile();
        customer.setUser(fixture.professional()); customer.setFirstNames("Profesional"); customer.setLastNames("Prueba"); em.persist(customer); em.flush();
        assertThatThrownBy(()->appointments.request(fixture.professional().getId(),input())).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("contigo mismo");
    }
    @Test void todayExcludesPastOrAlreadyStartedSlots() {
        var today=LocalDate.of(2026,11,5);
        appointments.addRule(fixture.professional().getId(),new RuleInput(4,LocalTime.of(6,0),LocalTime.of(9,0),today,today,null));
        assertThat(appointments.availability(service.getId(),today,SessionModality.ONLINE,null).slots())
            .extracting(Slot::startsAt).containsExactly(Instant.parse("2026-11-05T13:00:00Z"));
    }
    @Test void unpublishedOrHiddenProfessionalOffersNoAppointments() {
        service.setStatus(PublicationStatus.BORRADOR);
        assertThatThrownBy(()->appointments.options(service.getId())).isInstanceOf(ResourceNotFoundException.class);
        service.setStatus(PublicationStatus.PUBLICADO); service.getSpecialist().setShowOneToOneSessions(false);
        assertThatThrownBy(()->appointments.options(service.getId())).isInstanceOf(ResourceNotFoundException.class);
    }
    @Test void ruleValidationAndRemovalAreAppliedToAvailability() {
        assertThatThrownBy(()->appointments.addRule(fixture.professional().getId(),rule(null))).isInstanceOf(BookingConflictException.class);
        assertThatThrownBy(()->appointments.addRule(fixture.professional().getId(),new RuleInput(1,LocalTime.NOON,LocalTime.of(10,0),day,null,null))).isInstanceOf(IllegalArgumentException.class);
        var rule=appointments.rules(fixture.professional().getId()).stream().filter(a->a.venue()==null).findFirst().orElseThrow();
        appointments.removeRule(fixture.professional().getId(),rule.id());
        assertThat(appointments.availability(service.getId(),day,SessionModality.ONLINE,null).slots()).isEmpty();
    }
    @Test void requiresAuthenticationAndRoles() throws Exception {
        mvc.perform(get("/api/v1/appointments/services/"+service.getId())).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/professional-agenda/availability").with(user(principal(fixture.client().getId(),"USER")))).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/appointments/services/"+service.getId()).with(user(principal(fixture.client().getId(),"USER")))).andExpect(status().isOk()).andExpect(header().string("Cache-Control","no-store"));
    }
    @Test void customerSeesTheirAppointmentChangeFromPendingToConfirmed() throws Exception {
        var receipt = request();
        var customer = user(principal(fixture.client().getId(), "USER"));
        String path = "/api/v1/appointments/requests/me";

        mvc.perform(get(path).with(customer))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$[0].code").value(receipt.code()))
                .andExpect(jsonPath("$[0].status").value("PENDIENTE"));
        appointments.decide(fixture.professional().getId(), receipt.id(), BookingStatus.CONFIRMADA, null);
        mvc.perform(get(path).with(customer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("CONFIRMADA"));

        var other = BookingFixtures.create(em, "client" + UUID.randomUUID().toString().substring(0, 5));
        mvc.perform(get(path).with(user(principal(other.client().getId(), "USER"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
    }

    @Test void apiValidatesInputAndReturnsCreatedReceipt() throws Exception {
        mvc.perform(post("/api/v1/appointments/requests").with(user(principal(fixture.client().getId(),"USER"))).contentType("application/json")
            .content("{}")) .andExpect(status().isBadRequest());
        String body="{\"serviceId\":"+service.getId()+",\"modality\":\"ONLINE\",\"startsAt\":\""+start+"\",\"reason\":\"Quiero trabajar mi bienestar\"}";
        mvc.perform(post("/api/v1/appointments/requests").with(user(principal(fixture.client().getId(),"USER"))).contentType("application/json").content(body))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("PENDIENTE")).andExpect(jsonPath("$.currency").value("PEN"));
        mvc.perform(post("/api/v1/appointments/requests").with(user(principal(fixture.client().getId(),"USER"))).contentType("application/json").content(body)).andExpect(status().isConflict());
    }
}
