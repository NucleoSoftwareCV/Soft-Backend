package com.hean.consigueventas.oonabe.campaign;

import com.hean.consigueventas.oonabe.auth.security.UserDetailsImpl;
import com.hean.consigueventas.oonabe.booking.dto.ProfessionalBookingDtos.Decision;
import com.hean.consigueventas.oonabe.booking.entity.EventBooking;
import com.hean.consigueventas.oonabe.booking.repository.EventBookingRepository;
import com.hean.consigueventas.oonabe.booking.service.ExpiredOrderReleaseService;
import com.hean.consigueventas.oonabe.booking.service.ProfessionalBookingService;
import com.hean.consigueventas.oonabe.campaign.domain.CampaignEntryStatus;
import com.hean.consigueventas.oonabe.campaign.domain.CampaignStatus;
import com.hean.consigueventas.oonabe.campaign.entity.PromoCampaign;
import com.hean.consigueventas.oonabe.campaign.entity.PromoCampaignEntry;
import com.hean.consigueventas.oonabe.campaign.entity.PromoCampaignEvent;
import com.hean.consigueventas.oonabe.campaign.repository.PromoCampaignEntryRepository;
import com.hean.consigueventas.oonabe.campaign.service.CampaignEntryService;
import com.hean.consigueventas.oonabe.category.entity.Category;
import com.hean.consigueventas.oonabe.common.enums.*;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import com.hean.consigueventas.oonabe.payment.dto.CheckoutItemDto;
import com.hean.consigueventas.oonabe.payment.dto.CheckoutRequestDto;
import com.hean.consigueventas.oonabe.payment.dto.PaymentProcessRequestDto;
import com.hean.consigueventas.oonabe.payment.dto.PaymentResponseDto;
import com.hean.consigueventas.oonabe.payment.repository.PurchaseOrderRepository;
import com.hean.consigueventas.oonabe.payment.service.PaymentService;
import com.hean.consigueventas.oonabe.profileCliente.entity.CustomerProfile;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import com.hean.consigueventas.oonabe.user.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.random.RandomGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PromoCampaignIntegrationTest {

    static final String SLUG = "sorteo-prueba";

    @Autowired EntityManager em;
    @Autowired MockMvc mvc;
    @Autowired PaymentService payments;
    @Autowired ProfessionalBookingService professionalBookings;
    @Autowired CampaignEntryService entryService;
    @Autowired PromoCampaignEntryRepository entries;
    @Autowired EventBookingRepository bookings;
    @Autowired PurchaseOrderRepository orders;
    @Autowired com.hean.consigueventas.oonabe.booking.repository.BookingStatusHistoryRepository history;
    @Autowired com.hean.consigueventas.oonabe.event.repository.EventOccurrenceRepository occurrences;
    @MockitoBean(name = "campaignDrawRandom") RandomGenerator drawRandom;
    @MockitoBean com.hean.consigueventas.oonabe.profileProfesional.service.LocalImageStorageService images;

    User professional;
    User client;
    Category category;
    SpecialistProfile specialist;
    PromoCampaign campaign;
    Event included;
    Event excluded;
    Event includedWhatsApp;

    @BeforeEach
    void setup() {
        professional = newUser("campaign-pro");
        client = newUser("campaign-cli");
        CustomerProfile customer = new CustomerProfile();
        customer.setUser(client);
        customer.setFirstNames("Cliente");
        customer.setLastNames("Sorteo");
        em.persist(customer);
        specialist = new SpecialistProfile();
        specialist.setUser(professional);
        specialist.setSlug("campaign-pro");
        specialist.setPublicName("Profesional Sorteo");
        specialist.setWhatsappPhone("+34600000000");
        specialist.setApprovalStatus(ApprovalStatus.APROBADO);
        specialist.setPublicationStatus(PublicationStatus.PUBLICADO);
        em.persist(specialist);
        category = new Category();
        category.setName("Categoria sorteo");
        em.persist(category);

        included = event("Zzsorteo incluido", EventPaymentMethod.ONLINE);
        excluded = event("Zzsorteo excluido", EventPaymentMethod.ONLINE);
        includedWhatsApp = event("Zzsorteo whatsapp", EventPaymentMethod.WHATSAPP);

        Instant now = Instant.now();
        campaign = campaign(SLUG, CampaignStatus.PUBLICADA, now.minus(Duration.ofDays(1)), now.plus(Duration.ofDays(7)));
        em.persist(new PromoCampaignEvent(campaign, included));
        em.persist(new PromoCampaignEvent(campaign, includedWhatsApp));
        em.flush();
    }

    // --- Catálogo ---------------------------------------------------------------------------

    @Test
    void campaignFilterReturnsOnlyIncludedPublishedEventsWithBadge() throws Exception {
        mvc.perform(get("/api/v1/events").param("campaignSlug", SLUG).param("search", "Zzsorteo").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[*].id", hasItem(included.getId().intValue())))
                .andExpect(jsonPath("$.content[*].id", not(hasItem(excluded.getId().intValue()))))
                .andExpect(jsonPath("$.content[0].campaignBadges[0].slug").value(SLUG))
                .andExpect(jsonPath("$.content[0].campaignBadges[0].label").value("Participa en el sorteo"));
    }

    @Test
    void campaignFilterCombinesWithExistingFilters() throws Exception {
        mvc.perform(get("/api/v1/events").param("campaignSlug", SLUG).param("search", "whatsapp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(includedWhatsApp.getId()));
        mvc.perform(get("/api/v1/events").param("campaignSlug", SLUG).param("categoryId", "999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void regularListingShowsBadgeOnlyOnIncludedEvents() throws Exception {
        mvc.perform(get("/api/v1/events").param("search", "Zzsorteo excluido"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(excluded.getId()))
                .andExpect(jsonPath("$.content[0].campaignBadges").isEmpty());
        mvc.perform(get("/api/v1/events/" + included.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.campaignBadges[0].slug").value(SLUG));
        mvc.perform(get("/api/v1/events/" + excluded.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.campaignBadges").isEmpty());
    }

    @Test
    void draftOrClosedCampaignsDoNotFilterOrBadge() throws Exception {
        Instant now = Instant.now();
        PromoCampaign draft = campaign("sorteo-borrador", CampaignStatus.BORRADOR, now.minus(Duration.ofDays(1)), now.plus(Duration.ofDays(1)));
        PromoCampaign closed = campaign("sorteo-cerrado", CampaignStatus.PUBLICADA, now.minus(Duration.ofDays(9)), now.minus(Duration.ofDays(2)));
        em.persist(new PromoCampaignEvent(draft, excluded));
        em.persist(new PromoCampaignEvent(closed, excluded));
        em.flush();

        for (String slug : List.of("sorteo-borrador", "sorteo-cerrado", "no-existe")) {
            mvc.perform(get("/api/v1/events").param("campaignSlug", slug))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }
        mvc.perform(get("/api/v1/events/" + excluded.getId()))
                .andExpect(jsonPath("$.campaignBadges").isEmpty());
        mvc.perform(get("/api/v1/events").param("campaignSlug", "Slug Invalido"))
                .andExpect(status().isBadRequest());
    }

    // --- Endpoints públicos -----------------------------------------------------------------

    @Test
    void publicEndpointsExposePublishedCampaignsOnly() throws Exception {
        campaign("sorteo-oculto", CampaignStatus.BORRADOR, Instant.now(), Instant.now().plus(Duration.ofDays(3)));
        em.flush();

        mvc.perform(get("/api/v1/campaigns/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].slug", hasItem(SLUG)))
                .andExpect(jsonPath("$[*].slug", not(hasItem("sorteo-oculto"))));
        mvc.perform(get("/api/v1/campaigns/" + SLUG))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.prizeAmount").value(600.0))
                .andExpect(jsonPath("$.summary.phase").value("ABIERTA"))
                .andExpect(jsonPath("$.eligibleEventCount").value(2))
                .andExpect(jsonPath("$.faqs[0].question").value("¿Quién participa?"));
        mvc.perform(get("/api/v1/campaigns/sorteo-oculto"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/campaigns/" + SLUG + "/entries/me"))
                .andExpect(status().isUnauthorized());
    }

    // --- Administración ---------------------------------------------------------------------

    @Test
    void adminEndpointsRequireAdminAndValidateInput() throws Exception {
        mvc.perform(get("/api/v1/admin/campaigns").with(user(principal(client.getId(), "ROLE_USER"))))
                .andExpect(status().isForbidden());

        UserDetailsImpl admin = principal(professional.getId(), "ROLE_ADMIN");
        mvc.perform(post("/api/v1/admin/campaigns").with(user(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(upsertJson("sorteo-nuevo", "2026-11-10T00:00:00Z", "2026-11-01T00:00:00Z", "600")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/admin/campaigns").with(user(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(upsertJson(SLUG, "2026-11-01T00:00:00Z", "2026-11-10T00:00:00Z", "600")))
                .andExpect(status().isBadRequest());

        String created = mvc.perform(post("/api/v1/admin/campaigns").with(user(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(upsertJson("sorteo-nuevo", "2026-11-01T00:00:00Z", "2026-11-10T00:00:00Z", "600")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("BORRADOR"))
                .andExpect(jsonPath("$.detail.summary.badgeLabel").value("Participa en el sorteo"))
                .andReturn().getResponse().getContentAsString();
        long id = Long.parseLong(created.replaceAll(".*?\"id\":(\\d+).*", "$1"));

        mvc.perform(put("/api/v1/admin/campaigns/" + id + "/events").with(user(admin))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"eventIds\":[999999]}"))
                .andExpect(status().isNotFound());
        mvc.perform(put("/api/v1/admin/campaigns/" + id + "/events").with(user(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventIds\":[" + excluded.getId() + "," + excluded.getId() + "]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventIds.length()").value(1))
                .andExpect(jsonPath("$.detail.eligibleEventCount").value(1));
        mvc.perform(patch("/api/v1/admin/campaigns/" + id + "/status").with(user(admin))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"PUBLICADA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLICADA"));
    }

    // --- Participaciones --------------------------------------------------------------------

    @Test
    void approvedOnlinePaymentRegistersOneEntryPerSeatWithoutDuplicates() throws Exception {
        String orderCode = order(included, 2);
        PaymentResponseDto response = payments.processPayment(orderCode, card());

        assertThat(response.getStatus()).isEqualTo("APROBADO");
        assertThat(response.getCampaignEntries()).singleElement()
                .satisfies(e -> {
                    assertThat(e.campaignSlug()).isEqualTo(SLUG);
                    assertThat(e.ticketCount()).isEqualTo(2);
                });

        EventBooking booking = bookingOf(orderCode);
        assertThat(entryService.registerForConfirmedBookings(List.of(booking), Instant.now())).isEmpty();
        assertThat(entriesOf(booking)).singleElement()
                .satisfies(e -> assertThat(e.getStatus()).isEqualTo(CampaignEntryStatus.VALIDA));

        mvc.perform(get("/api/v1/campaigns/" + SLUG + "/entries/me").with(user(principal(client.getId(), "ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.validTickets").value(2))
                .andExpect(jsonPath("$.entries[0].bookingCode").value(booking.getCode()));
    }

    @Test
    void paymentForNonIncludedEventOrClosedCampaignRegistersNothing() {
        PaymentResponseDto notIncluded = payments.processPayment(order(excluded, 1), card());
        assertThat(notIncluded.getStatus()).isEqualTo("APROBADO");
        assertThat(notIncluded.getCampaignEntries()).isEmpty();

        campaign.setParticipationEndsAt(Instant.now().minus(Duration.ofMinutes(1)));
        em.flush();
        PaymentResponseDto closed = payments.processPayment(order(included, 1), card());
        assertThat(closed.getStatus()).isEqualTo("APROBADO");
        assertThat(closed.getCampaignEntries()).isEmpty();
    }

    @Test
    void rejectedPaymentRegistersNothing() {
        PaymentProcessRequestDto rejectedCard = card();
        rejectedCard.setCardNumber("4111111111114444");
        String orderCode = order(included, 1);

        assertThat(payments.processPayment(orderCode, rejectedCard).getStatus()).isEqualTo("RECHAZADO");
        assertThat(entriesOf(bookingOf(orderCode))).isEmpty();
    }

    @Test
    void perCustomerLimitCapsTickets() {
        campaign.setMaxEntriesPerCustomer(3);
        em.flush();

        assertThat(payments.processPayment(order(included, 2), card()).getCampaignEntries())
                .singleElement().satisfies(e -> assertThat(e.ticketCount()).isEqualTo(2));
        assertThat(payments.processPayment(order(included, 2), card()).getCampaignEntries())
                .singleElement().satisfies(e -> assertThat(e.ticketCount()).isEqualTo(1));
        assertThat(payments.processPayment(order(included, 1), card()).getCampaignEntries()).isEmpty();
    }

    @Test
    void whatsAppBookingEntersWhenProfessionalConfirmsAndIsVoidedWhenOrderExpires() {
        String orderCode = order(includedWhatsApp, 3);
        EventBooking booking = bookingOf(orderCode);
        assertThat(entriesOf(booking)).isEmpty();

        professionalBookings.decide(professional.getId(), booking.getCode(), new Decision(BookingStatus.CONFIRMADA, null));
        assertThat(entriesOf(booking)).singleElement().satisfies(e -> {
            assertThat(e.getTicketCount()).isEqualTo(3);
            assertThat(e.getStatus()).isEqualTo(CampaignEntryStatus.VALIDA);
        });

        var futureRelease = new ExpiredOrderReleaseService(orders, bookings, occurrences, history, entryService,
                Clock.offset(Clock.systemUTC(), Duration.ofHours(1)));
        assertThat(futureRelease.release(orderCode)).isEqualTo(1);
        em.flush();
        em.clear();
        assertThat(entriesOf(booking)).singleElement().satisfies(e -> {
            assertThat(e.getStatus()).isEqualTo(CampaignEntryStatus.ANULADA);
            assertThat(e.getVoidReason()).isEqualTo("Orden expirada sin pago");
        });
    }

    @Test
    void professionalRejectionCreatesNoEntry() {
        String orderCode = order(includedWhatsApp, 1);
        EventBooking booking = bookingOf(orderCode);

        professionalBookings.decide(professional.getId(), booking.getCode(), new Decision(BookingStatus.CANCELADA, "Sin plazas"));
        assertThat(entriesOf(booking)).isEmpty();
    }

    // --- Gestión: reglas de edición, estados y borrado --------------------------------------

    @Test
    void campaignWithEntriesProtectsSensitiveFieldsAndEvents() throws Exception {
        payments.processPayment(order(included, 1), card());
        UserDetailsImpl admin = principal(professional.getId(), "ROLE_ADMIN");
        String path = "/api/v1/admin/campaigns/" + campaign.getId();
        String start = campaign.getParticipationStartsAt().toString();
        String end = campaign.getParticipationEndsAt().toString();
        String later = campaign.getParticipationEndsAt().plus(Duration.ofDays(3)).toString();

        mvc.perform(get(path).with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locked").value(true))
                .andExpect(jsonPath("$.entryCount").value(1))
                .andExpect(jsonPath("$.validTickets").value(1))
                .andExpect(jsonPath("$.participantCount").value(1))
                .andExpect(jsonPath("$.events[*].title", hasItem("Zzsorteo incluido")));

        mvc.perform(put(path).with(user(admin)).contentType(MediaType.APPLICATION_JSON)
                        .content(upsertJson("otro-slug", start, end, "600")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("La campaña ya tiene participaciones: no se puede cambiar el slug"));
        mvc.perform(put(path).with(user(admin)).contentType(MediaType.APPLICATION_JSON)
                        .content(upsertJson(SLUG, start, end, "900")))
                .andExpect(status().isBadRequest());
        mvc.perform(put(path).with(user(admin)).contentType(MediaType.APPLICATION_JSON)
                        .content(upsertJson(SLUG, start, Instant.now().plus(Duration.ofDays(1)).toString(), "600")))
                .andExpect(status().isBadRequest());
        mvc.perform(put(path).with(user(admin)).contentType(MediaType.APPLICATION_JSON)
                        .content(upsertJson(SLUG, start, later, "600")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detail.summary.title").value("Sorteo"));

        mvc.perform(put(path + "/events").with(user(admin)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventIds\":[" + includedWhatsApp.getId() + "]}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put(path + "/events").with(user(admin)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventIds\":[" + included.getId() + "," + includedWhatsApp.getId() + "," + excluded.getId() + "]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventIds.length()").value(3));

        mvc.perform(patch(path + "/status").with(user(admin)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"BORRADOR\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(delete(path).with(user(admin)))
                .andExpect(status().isBadRequest());
        mvc.perform(patch(path + "/status").with(user(admin)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"FINALIZADA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINALIZADA"));
        mvc.perform(put(path).with(user(admin)).contentType(MediaType.APPLICATION_JSON)
                        .content(upsertJson(SLUG, start, later, "600")))
                .andExpect(status().isBadRequest());
        mvc.perform(patch(path + "/status").with(user(admin)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PUBLICADA\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void draftRulesForPublishingIncludingEventsAndDeleting() throws Exception {
        UserDetailsImpl admin = principal(professional.getId(), "ROLE_ADMIN");
        PromoCampaign draft = campaign("sorteo-borrador", CampaignStatus.BORRADOR,
                Instant.now(), Instant.now().plus(Duration.ofDays(5)));
        Event hidden = event("Zzsorteo oculto", EventPaymentMethod.ONLINE);
        hidden.setStatus(EventStatus.BORRADOR);
        em.flush();
        String path = "/api/v1/admin/campaigns/" + draft.getId();

        mvc.perform(patch(path + "/status").with(user(admin)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PUBLICADA\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Añade al menos una experiencia antes de publicar la campaña"));
        mvc.perform(put(path + "/events").with(user(admin)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventIds\":[" + hidden.getId() + "]}"))
                .andExpect(status().isBadRequest());

        mvc.perform(delete(path).with(user(admin)))
                .andExpect(status().isNoContent());
        mvc.perform(get(path).with(user(admin)))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/v1/admin/campaigns/" + campaign.getId()).with(user(admin)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadsHeroImageReplacingThePreviousOneAndOnlyForAdmins() throws Exception {
        UserDetailsImpl admin = principal(professional.getId(), "ROLE_ADMIN");
        PromoCampaign draft = campaign("sorteo-imagen", CampaignStatus.BORRADOR,
                Instant.now(), Instant.now().plus(Duration.ofDays(5)));
        draft.setHeroImageUrl("/images/profile-images/1/campaign-hero/antigua.jpg");
        em.flush();
        String path = "/api/v1/admin/campaigns/" + draft.getId();
        MockMultipartFile file = new MockMultipartFile("file", "hero.png", "image/png", new byte[] {1});
        when(images.saveCampaignImage(any(), eq(draft.getId())))
                .thenReturn("/images/profile-images/1/campaign-hero/nueva.png");

        mvc.perform(multipart(path + "/image").file(file).with(user(principal(client.getId(), "ROLE_USER"))))
                .andExpect(status().isForbidden());
        mvc.perform(multipart(path + "/image").file(file).with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detail.summary.heroImageUrl")
                        .value("/images/profile-images/1/campaign-hero/nueva.png"));
        verify(images).deleteImage("/images/profile-images/1/campaign-hero/antigua.jpg");

        mvc.perform(delete(path).with(user(admin)))
                .andExpect(status().isNoContent());
        verify(images).deleteImage("/images/profile-images/1/campaign-hero/nueva.png");

        PromoCampaign finished = campaign("sorteo-acabado", CampaignStatus.FINALIZADA,
                Instant.now().minus(Duration.ofDays(5)), Instant.now().minus(Duration.ofDays(1)));
        em.flush();
        mvc.perform(multipart("/api/v1/admin/campaigns/" + finished.getId() + "/image").file(file).with(user(admin)))
                .andExpect(status().isBadRequest());
    }

    // --- Sorteo -----------------------------------------------------------------------------

    @Test
    void drawPicksEntryWeightedByTicketsFinalizesAndPublishesWinner() throws Exception {
        payments.processPayment(order(included, 2), card());
        String secondOrder = order(included, 3);
        payments.processPayment(secondOrder, card());
        campaign.setParticipationEndsAt(Instant.now().minus(Duration.ofMinutes(1)));
        em.flush();
        // Papeletas 0-1 → primera reserva, 2-4 → segunda.
        when(drawRandom.nextLong(anyLong())).thenReturn(2L);
        UserDetailsImpl admin = principal(professional.getId(), "ROLE_ADMIN");
        String path = "/api/v1/admin/campaigns/" + campaign.getId();

        mvc.perform(post(path + "/draw").with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINALIZADA"))
                .andExpect(jsonPath("$.validTickets").value(5))
                .andExpect(jsonPath("$.winner.bookingCode").value(bookingOf(secondOrder).getCode()))
                .andExpect(jsonPath("$.winner.ticketCount").value(3))
                .andExpect(jsonPath("$.winner.customerEmail").value("campaign-cli@example.com"));
        verify(drawRandom).nextLong(5L);

        mvc.perform(post(path + "/draw").with(user(admin)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("El sorteo de esta campaña ya se realizó"));
        mvc.perform(get("/api/v1/campaigns/" + SLUG))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.winnerDisplayName").value("Cliente S."))
                .andExpect(jsonPath("$.drawnAt").isNotEmpty());
        mvc.perform(post(path + "/draw").with(user(principal(client.getId(), "ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void drawRequiresClosedPublishedCampaignWithValidEntries() throws Exception {
        UserDetailsImpl admin = principal(professional.getId(), "ROLE_ADMIN");

        payments.processPayment(order(included, 1), card());
        mvc.perform(post("/api/v1/admin/campaigns/" + campaign.getId() + "/draw").with(user(admin)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("El sorteo solo puede realizarse cuando cierre el periodo de participación"));

        PromoCampaign empty = campaign("sorteo-vacio", CampaignStatus.PUBLICADA,
                Instant.now().minus(Duration.ofDays(5)), Instant.now().minus(Duration.ofDays(1)));
        em.flush();
        mvc.perform(post("/api/v1/admin/campaigns/" + empty.getId() + "/draw").with(user(admin)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("La campaña no tiene participaciones válidas para sortear"));
    }

    // --- Varias campañas --------------------------------------------------------------------

    @Test
    void overlappingCampaignsEachReceiveEntriesAndActiveListsOpenFirstByClosing() throws Exception {
        Instant now = Instant.now();
        PromoCampaign sooner = campaign("sorteo-flash", CampaignStatus.PUBLICADA,
                now.minus(Duration.ofHours(1)), now.plus(Duration.ofDays(2)));
        campaign("sorteo-futuro", CampaignStatus.PUBLICADA, now.plus(Duration.ofDays(1)), now.plus(Duration.ofDays(3)));
        em.persist(new PromoCampaignEvent(sooner, included));
        em.flush();

        assertThat(payments.processPayment(order(included, 1), card()).getCampaignEntries())
                .extracting(e -> e.campaignSlug())
                .containsExactlyInAnyOrder(SLUG, "sorteo-flash");

        mvc.perform(get("/api/v1/campaigns/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slug").value("sorteo-flash"))
                .andExpect(jsonPath("$[1].slug").value(SLUG))
                .andExpect(jsonPath("$[2].slug").value("sorteo-futuro"))
                .andExpect(jsonPath("$[2].phase").value("PROXIMA"));
    }

    // --- Helpers ----------------------------------------------------------------------------

    private String order(Event event, int quantity) {
        CheckoutItemDto item = new CheckoutItemDto();
        item.setItemType(PurchaseItemType.EVENTO);
        item.setReferenceId(event.getOccurrences().getFirst().getId());
        item.setQuantity((short) quantity);
        CheckoutRequestDto request = new CheckoutRequestDto();
        request.setItems(List.of(item));
        return payments.createOrder(client.getId(), request).getCode();
    }

    private EventBooking bookingOf(String orderCode) {
        return bookings.findLockedByOrderId(orders.findByCode(orderCode).orElseThrow().getId()).getFirst();
    }

    private List<PromoCampaignEntry> entriesOf(EventBooking booking) {
        return entries.findAll().stream().filter(e -> e.getBooking().getId().equals(booking.getId())).toList();
    }

    private static PaymentProcessRequestDto card() {
        PaymentProcessRequestDto request = new PaymentProcessRequestDto();
        request.setMethod("TARJETA");
        request.setCardNumber("4111111111111111");
        request.setCardExpiration("12/30");
        request.setCardCvv("123");
        return request;
    }

    private Event event(String title, EventPaymentMethod paymentMethod) {
        Event event = new Event();
        event.setTitle(title);
        event.setDescription("Evento para verificar campañas");
        event.setCategory(category);
        event.setSpecialist(specialist);
        event.setModality(EventModality.ONLINE);
        event.setPriceFrom(new BigDecimal("20.00"));
        event.setStatus(EventStatus.PUBLICADO);
        event.setPaymentMethod(paymentMethod);
        em.persist(event);
        EventOccurrence occurrence = new EventOccurrence();
        occurrence.setEvent(event);
        occurrence.setCapacity(20);
        occurrence.setReservedSpots(0);
        occurrence.setStartsAt(Instant.now().plus(Duration.ofDays(2)));
        occurrence.setEndsAt(Instant.now().plus(Duration.ofDays(2)).plus(Duration.ofHours(1)));
        occurrence.setStatus(EventOccurrenceStatus.PROGRAMADA);
        em.persist(occurrence);
        event.setOccurrences(new java.util.ArrayList<>(List.of(occurrence)));
        return event;
    }

    private PromoCampaign campaign(String slug, CampaignStatus status, Instant startsAt, Instant endsAt) {
        PromoCampaign c = new PromoCampaign();
        c.setSlug(slug);
        c.setTitle("Sorteo de prueba");
        c.setDescription("Campaña de prueba");
        c.setBadgeLabel("Participa en el sorteo");
        c.setPrizeAmount(new BigDecimal("600.00"));
        c.setParticipationStartsAt(startsAt);
        c.setParticipationEndsAt(endsAt);
        c.setStatus(status);
        c.getFaqs().add(new com.hean.consigueventas.oonabe.campaign.entity.CampaignFaq("¿Quién participa?", "Clientes con reserva."));
        em.persist(c);
        return c;
    }

    private User newUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        em.persist(user);
        return user;
    }

    private static UserDetailsImpl principal(Long id, String role) {
        return new UserDetailsImpl(id, "test", "test@example.com", "", true, List.of(new SimpleGrantedAuthority(role)));
    }

    private static String upsertJson(String slug, String startsAt, String endsAt, String prize) {
        return """
                {"slug":"%s","title":"Sorteo","description":"Descripción","prizeAmount":%s,
                 "participationStartsAt":"%s","participationEndsAt":"%s"}
                """.formatted(slug, prize, startsAt, endsAt);
    }
}
