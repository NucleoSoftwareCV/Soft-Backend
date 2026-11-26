package com.hean.consigueventas.oonabe.support;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hean.consigueventas.oonabe.auth.security.UserDetailsImpl;
import com.hean.consigueventas.oonabe.experienceType.entity.ExperienceType;
import com.hean.consigueventas.oonabe.payment.domain.PaymentStatus;
import com.hean.consigueventas.oonabe.payment.entity.Payment;
import com.hean.consigueventas.oonabe.payment.entity.PurchaseOrder;
import com.hean.consigueventas.oonabe.profileCliente.entity.CustomerProfile;
import com.hean.consigueventas.oonabe.rewards.Entity.EarnPointsRules;
import com.hean.consigueventas.oonabe.rewards.Entity.PointsTransactions;
import com.hean.consigueventas.oonabe.rewards.Entity.UserPoints;
import com.hean.consigueventas.oonabe.rewards.Enum.TransactionType;
import com.hean.consigueventas.oonabe.rewards.Enum.UserPointStatus;
import com.hean.consigueventas.oonabe.support.entity.*;
import com.hean.consigueventas.oonabe.support.enums.*;
import com.hean.consigueventas.oonabe.support.repository.*;
import com.hean.consigueventas.oonabe.user.entity.*;
import com.hean.consigueventas.oonabe.user.repository.*;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SupportIntegrationTest {
    private static final String BASE = "/api/v1/support";
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired SupportTicketRepository tickets;
    @Autowired RefundRequestRepository refunds;
    @Autowired EntityManager em;
    private User owner, other, operator, admin;
    private Payment payment;

    @BeforeEach
    void prepare() {
        owner = account("sup-owner", "ROLE_USER");
        other = account("sup-other", "ROLE_USER");
        operator = account("sup-agent", "ROLE_SUPPORT");
        admin = account("sup-admin", "ROLE_ADMIN");
        CustomerProfile profile = new CustomerProfile();
        profile.setUser(owner);
        profile.setFirstNames("Soporte");
        profile.setLastNames("Prueba");
        em.persist(profile);
        PurchaseOrder order = new PurchaseOrder();
        order.setCustomer(profile);
        order.setCode("SUP-" + UUID.randomUUID());
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setCurrency("EUR");
        em.persist(order);
        payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(new BigDecimal("100.00"));
        payment.setCurrency("EUR");
        payment.setProvider("SUPPORT_TEST_NO_GATEWAY");
        payment.setExternalReference(UUID.randomUUID().toString());
        payment.setStatus(PaymentStatus.APROBADO);
        em.persist(payment);
        em.flush();
    }

    private User account(String name, String roleName) {
        Role role =
                roles
                        .findByName(roleName)
                        .orElseGet(
                                () -> {
                                    Role r = new Role();
                                    r.setName(roleName);
                                    r.setDisplayName(roleName);
                                    return roles.saveAndFlush(r);
                                });
        User u = new User();
        u.setUsername(name);
        u.setEmail(name + "@support.test");
        u.setPassword("not-used-by-mock-authentication");
        u.getRoles().add(role);
        return users.saveAndFlush(u);
    }

    private MockHttpServletRequestBuilder as(MockHttpServletRequestBuilder request, User u) {
        return request.with(user(UserDetailsImpl.build(u))).contentType("application/json");
    }

    private long ticket() throws Exception {
        String response =
                mvc.perform(
                                as(post(BASE + "/tickets"), owner)
                                        .content(
                                                json.writeValueAsString(
                                                        Map.of(
                                                                "category",
                                                                "PAGOS",
                                                                "subject",
                                                                "Pago por revisar",
                                                                "description",
                                                                "Descripción de prueba"))))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.ticketNumber", startsWith("OONA-")))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        return json.readTree(response).get("id").asLong();
    }

    private long refund(String amount, User requester) throws Exception {
        String response =
                mvc.perform(
                                as(post(BASE + "/refunds"), requester)
                                        .content(
                                                json.writeValueAsString(
                                                        Map.of(
                                                                "transactionId",
                                                                payment.getId(),
                                                                "requestedAmount",
                                                                amount,
                                                                "reason",
                                                                "EVENT_CANCELLED",
                                                                "description",
                                                                "Evento cancelado"))))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        return json.readTree(response).get("id").asLong();
    }

    @Test
    void authenticationRequired() throws Exception {
        mvc.perform(get(BASE + "/tickets/my")).andExpect(status().isUnauthorized());
    }

    @Test
    void ownerCanCreateAndReadTicket() throws Exception {
        long id = ticket();
        mvc.perform(as(get(BASE + "/tickets/" + id), owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDIENTE"));
    }

    @Test
    void anotherUserCannotReadTicket() throws Exception {
        long id = ticket();
        mvc.perform(as(get(BASE + "/tickets/" + id), other)).andExpect(status().isForbidden());
    }

    @Test
    void usersCannotReadGlobalQueue() throws Exception {
        mvc.perform(as(get(BASE + "/admin/tickets"), owner)).andExpect(status().isForbidden());
    }

    @Test
    void internalNotesAndTheirAuditArePrivate() throws Exception {
        long id = ticket();
        mvc.perform(
                        as(post(BASE + "/admin/tickets/" + id + "/responses"), admin)
                                .content("{\"message\":\"Confidencial del equipo\",\"internal\":true}"))
                .andExpect(status().isOk());
        mvc.perform(as(get(BASE + "/tickets/" + id), owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responses", hasSize(0)))
                .andExpect(content().string(not(containsString("NOTA_INTERNA"))))
                .andExpect(content().string(not(containsString("Confidencial del equipo"))));
    }

    @Test
    void userCannotForgeInternalNote() throws Exception {
        long id = ticket();
        mvc.perform(
                        as(post(BASE + "/tickets/" + id + "/responses"), owner)
                                .content("{\"message\":\"Nota\",\"internal\":true}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generalAdminCanOperateTickets() throws Exception {
        long id = ticket();
        mvc.perform(
                        as(post(BASE + "/admin/tickets/" + id + "/responses"), admin)
                                .content("{\"message\":\"Respuesta\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void formerAgentCannotAccessQueue() throws Exception {
        var stalePrincipal = UserDetailsImpl.build(operator);
        mvc.perform(get(BASE + "/admin/tickets").with(user(stalePrincipal)))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidStatusTransitionRejected() throws Exception {
        long id = ticket();
        mvc.perform(
                        as(patch(BASE + "/admin/tickets/" + id + "/status"), admin)
                                .content("{\"status\":\"CERRADO\",\"reason\":\"Cierre directo\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ticketReopensOnlyAfterResolution() throws Exception {
        long id = ticket();
        mvc.perform(
                        as(post(BASE + "/tickets/" + id + "/reopen"), owner)
                                .content("{\"reason\":\"Persiste\"}"))
                .andExpect(status().isBadRequest());
        var t = tickets.findById(id).orElseThrow();
        t.setStatus(TicketStatus.RESUELTO);
        tickets.saveAndFlush(t);
        mvc.perform(
                        as(post(BASE + "/tickets/" + id + "/reopen"), owner)
                                .content("{\"reason\":\"Persiste\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REABIERTO"));
    }

    @Test
    void cannotRefundAnotherUsersPayment() throws Exception {
        mvc.perform(
                        as(post(BASE + "/refunds"), other)
                                .content(
                                        json.writeValueAsString(
                                                Map.of(
                                                        "transactionId",
                                                        payment.getId(),
                                                        "requestedAmount",
                                                        10,
                                                        "reason",
                                                        "OTHER",
                                                        "description",
                                                        "Prueba"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void cannotRequestMoreThanPayment() throws Exception {
        mvc.perform(
                        as(post(BASE + "/refunds"), owner)
                                .content(
                                        json.writeValueAsString(
                                                Map.of(
                                                        "transactionId",
                                                        payment.getId(),
                                                        "requestedAmount",
                                                        101,
                                                        "reason",
                                                        "OTHER",
                                                        "description",
                                                        "Prueba"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void outstandingRefundReservesEligibleAmount() throws Exception {
        refund("80.00", owner);
        mvc.perform(
                        as(post(BASE + "/refunds"), owner)
                                .content(
                                        json.writeValueAsString(
                                                Map.of(
                                                        "transactionId",
                                                        payment.getId(),
                                                        "requestedAmount",
                                                        30,
                                                        "reason",
                                                        "OTHER",
                                                        "description",
                                                        "Prueba"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ownerCanLinkLegacyTicketAndSeeAdminApproval() throws Exception {
        long ticketId = ticket();
        var legacyTicket = tickets.findById(ticketId).orElseThrow();
        legacyTicket.setCategory(com.hean.consigueventas.oonabe.support.enums.TicketCategory.REEMBOLSOS);
        tickets.saveAndFlush(legacyTicket);

        String result = mvc.perform(as(post(BASE + "/refunds"), owner)
                        .content(json.writeValueAsString(Map.of(
                                "ticketId", ticketId,
                                "transactionId", payment.getId(),
                                "requestedAmount", "50.00",
                                "reason", "EVENT_CANCELLED",
                                "description", "Solicitud vinculada"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketId").value(ticketId))
                .andReturn().getResponse().getContentAsString();
        long refundId = json.readTree(result).get("id").asLong();

        mvc.perform(as(patch(BASE + "/admin/refunds/" + refundId + "/approve"), admin)
                        .content("{\"approvedAmount\":50,\"reason\":\"Solicitud evaluada\"}"))
                .andExpect(status().isOk());
        mvc.perform(as(get(BASE + "/refunds/my"), owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].ticketId").value(ticketId))
                .andExpect(jsonPath("$.content[0].status").value("APROBADO"));
    }

    @Test
    void refundDecisionAppearsOnlyInOwnersUnreadNotifications() throws Exception {
        long id = refund("50.00", owner);
        mvc.perform(as(patch(BASE + "/admin/refunds/" + id + "/reject"), admin)
                        .content("{\"reason\":\"La solicitud no cumple las condiciones\"}"))
                .andExpect(status().isOk());

        var ticket = refunds.findById(id).orElseThrow().getTicket();
        assertEquals(TicketStatus.CERRADO, ticket.getStatus());

        String endpoint = "/api/v1/notifications/me/refund-decisions";
        String result = mvc.perform(as(get(endpoint), owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].type").value("REEMBOLSO_RECHAZADO"))
                .andExpect(jsonPath("$.content[0].bookingCode").value(Long.toString(id)))
                .andReturn().getResponse().getContentAsString();
        long notificationId = json.readTree(result).get("content").get(0).get("id").asLong();

        mvc.perform(as(get(endpoint), other))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
        mvc.perform(as(patch("/api/v1/notifications/" + notificationId + "/read"), owner))
                .andExpect(status().isOk());
        mvc.perform(as(get(endpoint), owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void rewardsInspectionRestrictsAccessAndFiltersRedemptions() throws Exception {
        long id = ticket();
        var ticket = tickets.findById(id).orElseThrow();
        ticket.setCategory(com.hean.consigueventas.oonabe.support.enums.TicketCategory.CANJES);
        tickets.saveAndFlush(ticket);
        String endpoint = BASE + "/admin/tickets/" + id + "/rewards";

        var now = LocalDateTime.now();
        var experienceType = new ExperienceType();
        experienceType.setName("Soporte Rewards");
        em.persist(experienceType);
        var rule = EarnPointsRules.builder().reward(100).active(true).experienceType(experienceType).build();
        em.persist(rule);
        var points = UserPoints.builder().user(owner).availablePoints(70)
                .status(UserPointStatus.ACTIVE).updatedAt(now).build();
        em.persist(points);
        em.persist(PointsTransactions.builder().userPoints(points).earnPointsRules(rule)
                .type(TransactionType.EARN).pointsQuantity(100).balanceAfter(100).createdAt(now.minusDays(1)).build());
        em.persist(PointsTransactions.builder().userPoints(points).earnPointsRules(rule)
                .type(TransactionType.REDEEM).pointsQuantity(30).balanceAfter(70).createdAt(now).build());
        em.flush();

        mvc.perform(as(get(endpoint), owner)).andExpect(status().isForbidden());
        mvc.perform(as(get(endpoint), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(70))
                .andExpect(jsonPath("$.movements.content", hasSize(1)))
                .andExpect(jsonPath("$.movements.content[0].type").value("REDEEM"))
                .andExpect(jsonPath("$.movements.content[0].points").value(30));
    }

    @Test
    void refundDecisionRequiresAdmin() throws Exception {
        long id = refund("50.00", owner);
        mvc.perform(
                        as(patch(BASE + "/admin/refunds/" + id + "/approve"), operator)
                                .content("{\"approvedAmount\":50,\"reason\":\"Condiciones revisadas\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void approvalRequiresReasonAndRespectsRequestedAmount() throws Exception {
        long id = refund("50.00", owner);
        mvc.perform(
                        as(patch(BASE + "/admin/refunds/" + id + "/approve"), admin)
                                .content("{\"approvedAmount\":50,\"reason\":\"\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(
                        as(patch(BASE + "/admin/refunds/" + id + "/approve"), admin)
                                .content("{\"approvedAmount\":51,\"reason\":\"Revisado\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approvalClosesTicketWithoutChangingPayment() throws Exception {
        long id = refund("50.00", owner);
        var request = refunds.findById(id).orElseThrow();
        Long ticketId = request.getTicket().getId();
        mvc.perform(as(patch(BASE + "/admin/refunds/" + id + "/approve"), admin)
                        .content("{\"approvedAmount\":50,\"reason\":\"Condiciones revisadas\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROBADO"));
        assertEquals(TicketStatus.CERRADO, tickets.findById(ticketId).orElseThrow().getStatus());
        assertEquals(PaymentStatus.APROBADO, payment.getStatus());
        assertNull(request.getProcessedAt());
    }

    @Test
    void evidenceDownloadRequiresTicketOwnership() throws Exception {
        long id = ticket();
        var file =
                new MockMultipartFile(
                        "file", "evidencia.pdf", "application/pdf", "%PDF-1.7\nEvidencia de prueba".getBytes());
        var result =
                mvc.perform(
                                multipart(BASE + "/tickets/" + id + "/attachments")
                                        .file(file)
                                        .with(user(UserDetailsImpl.build(owner))))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.filePath").doesNotExist())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        long attachmentId = json.readTree(result).get("id").asLong();
        mvc.perform(as(get(BASE + "/attachments/" + attachmentId), other))
                .andExpect(status().isForbidden());
        mvc.perform(as(get(BASE + "/attachments/" + attachmentId), owner))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"));
    }

    @Test
    void evidenceRejectsDisguisedExecutable() throws Exception {
        long id = ticket();
        var file =
                new MockMultipartFile("file", "falso.pdf", "application/pdf", "MZ-ejecutable".getBytes());
        mvc.perform(
                        multipart(BASE + "/tickets/" + id + "/attachments")
                                .file(file)
                                .with(user(UserDetailsImpl.build(owner))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void evidenceRejectsMismatchedMimeType() throws Exception {
        long id = ticket();
        var file =
                new MockMultipartFile(
                        "file", "evidencia.pdf", "image/png", "%PDF-1.7\ncontenido".getBytes());
        mvc.perform(
                        multipart(BASE + "/tickets/" + id + "/attachments")
                                .file(file)
                                .with(user(UserDetailsImpl.build(owner))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void evidenceRejectsMoreThanFiveMegabytes() throws Exception {
        long id = ticket();
        var file =
                new MockMultipartFile(
                        "file", "grande.pdf", "application/pdf", new byte[5 * 1024 * 1024 + 1]);
        mvc.perform(
                        multipart(BASE + "/tickets/" + id + "/attachments")
                                .file(file)
                                .with(user(UserDetailsImpl.build(owner))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ticketIgnoresForgedUserId() throws Exception {
        mvc.perform(
                        as(post(BASE + "/tickets"), owner)
                                .content(
                                        json.writeValueAsString(
                                                Map.of(
                                                        "userId",
                                                        other.getId(),
                                                        "category",
                                                        "OTROS",
                                                        "subject",
                                                        "Solicitud",
                                                        "description",
                                                        "Prueba"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(owner.getId()));
    }

    private com.hean.consigueventas.oonabe.booking.entity.SessionBooking ownSession(User buyer) {
        var specs =
                em.createQuery(
                                "select s from SpecialistProfile s where s.user.id=:id",
                                com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile.class)
                        .setParameter("id", operator.getId())
                        .getResultList();
        com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile specialist;
        if (specs.isEmpty()) {
            specialist = new com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile();
            specialist.setUser(operator);
            specialist.setSlug("support-" + UUID.randomUUID());
            specialist.setPublicName("Profesional de prueba");
            specialist.setWhatsappPhone("000000000");
            em.persist(specialist);
        } else specialist = specs.getFirst();
        var service = new com.hean.consigueventas.oonabe.oneToOneSession.entity.OneToOneService();
        service.setSpecialist(specialist);
        service.setSlug("sesion-" + UUID.randomUUID());
        service.setTitle("Sesión de " + buyer.getUsername());
        service.setDescription("Servicio de prueba");
        service.setDurationMinutes(60);
        service.setPrice(new BigDecimal("50"));
        service.setModality(com.hean.consigueventas.oonabe.common.enums.SessionModality.ONLINE);
        em.persist(service);
        CustomerProfile customer;
        if (buyer == owner) customer = payment.getOrder().getCustomer();
        else {
            customer = new CustomerProfile();
            customer.setUser(buyer);
            customer.setFirstNames("Otro");
            customer.setLastNames("Usuario");
            em.persist(customer);
        }
        var booking = new com.hean.consigueventas.oonabe.booking.entity.SessionBooking();
        booking.setCode("SES-" + UUID.randomUUID().toString().substring(0, 20));
        booking.setCustomer(customer);
        booking.setService(service);
        booking.setSpecialist(specialist);
        booking.setStartsAt(java.time.Instant.now().plusSeconds(86400));
        booking.setEndsAt(booking.getStartsAt().plusSeconds(3600));
        booking.setModality(com.hean.consigueventas.oonabe.common.enums.SessionModality.ONLINE);
        booking.setPrice(new BigDecimal("50"));
        booking.setCurrency("EUR");
        em.persist(booking);
        em.flush();
        return booking;
    }

    @Test
    void referencePickerOnlyListsOwnReservations() throws Exception {
        var mine = ownSession(owner);
        ownSession(other);
        mvc.perform(as(get(BASE + "/references/my"), owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value(mine.getService().getTitle()))
                .andExpect(jsonPath("$[0].sessionBookingId").value(mine.getId()))
                .andExpect(jsonPath("$[0].code").value(mine.getCode()));
    }

    @Test
    void ticketLinksOwnSessionAndReturnsItsName() throws Exception {
        var mine = ownSession(owner);
        mvc.perform(
                        as(post(BASE + "/tickets"), owner)
                                .content(
                                        json.writeValueAsString(
                                                Map.of(
                                                        "category",
                                                        "RESERVAS",
                                                        "subject",
                                                        "Ayuda con mi sesión",
                                                        "description",
                                                        "Necesito ayuda",
                                                        "sessionBookingId",
                                                        mine.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionTitle").value(mine.getService().getTitle()))
                .andExpect(jsonPath("$.sessionBookingCode").value(mine.getCode()));
    }

    @Test
    void ticketCannotLinkAnotherUsersSession() throws Exception {
        var theirs = ownSession(other);
        mvc.perform(
                        as(post(BASE + "/tickets"), owner)
                                .content(
                                        json.writeValueAsString(
                                                Map.of(
                                                        "category",
                                                        "RESERVAS",
                                                        "subject",
                                                        "Ayuda",
                                                        "description",
                                                        "Prueba",
                                                        "sessionBookingId",
                                                        theirs.getId()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void emptyReferenceListDoesNotBlockGeneralTicket() throws Exception {
        mvc.perform(as(get(BASE + "/references/my"), owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        ticket();
    }

    @Test
    void ticketCannotLinkTwoKindsOfReservation() throws Exception {
        var mine = ownSession(owner);
        mvc.perform(
                        as(post(BASE + "/tickets"), owner)
                                .content(
                                        json.writeValueAsString(
                                                Map.of(
                                                        "category",
                                                        "RESERVAS",
                                                        "subject",
                                                        "Ayuda",
                                                        "description",
                                                        "Prueba",
                                                        "bookingId",
                                                        999,
                                                        "sessionBookingId",
                                                        mine.getId()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void eventReferenceWithoutOwnReservationIsRejected() throws Exception {
        mvc.perform(
                        as(post(BASE + "/tickets"), owner)
                                .content(
                                        json.writeValueAsString(
                                                Map.of(
                                                        "category",
                                                        "EVENTOS",
                                                        "subject",
                                                        "Ayuda",
                                                        "description",
                                                        "Prueba",
                                                        "eventId",
                                                        999))))
                .andExpect(status().isForbidden());
    }
}
