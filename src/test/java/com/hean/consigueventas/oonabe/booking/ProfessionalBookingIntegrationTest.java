package com.hean.consigueventas.oonabe.booking;

import com.hean.consigueventas.oonabe.auth.security.UserDetailsImpl;
import com.hean.consigueventas.oonabe.booking.dto.ProfessionalBookingDtos.Decision;
import com.hean.consigueventas.oonabe.booking.exception.BookingConflictException;
import com.hean.consigueventas.oonabe.booking.repository.*;
import com.hean.consigueventas.oonabe.booking.service.ExpiredOrderReleaseService;
import com.hean.consigueventas.oonabe.booking.service.ProfessionalBookingService;
import com.hean.consigueventas.oonabe.common.enums.*;
import com.hean.consigueventas.oonabe.payment.dto.*;
import com.hean.consigueventas.oonabe.payment.repository.PurchaseOrderRepository;
import com.hean.consigueventas.oonabe.payment.service.PaymentService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProfessionalBookingIntegrationTest {
    static final String BASE = "/api/v1/bookings/professional/me";
    @Autowired EntityManager em;
    @Autowired MockMvc mvc;
    @Autowired PaymentService payments;
    @Autowired ProfessionalBookingService service;
    @Autowired ExpiredOrderReleaseService release;
    @Autowired EventBookingRepository bookings;
    @Autowired PurchaseOrderRepository orders;
    @Autowired BookingStatusHistoryRepository history;
    BookingFixtures.Fixture fixture;
    String code;
    String orderCode;

    @BeforeEach void setup() {
        fixture = BookingFixtures.create(em, "primary");
        orderCode = payments.createOrder(fixture.client().getId(), checkout(fixture.occurrence().getId())).getCode();
        code = bookings.findLockedByOrderId(orders.findByCode(orderCode).orElseThrow().getId()).getFirst().getCode();
    }

    static CheckoutRequestDto checkout(Long occurrenceId) {
        CheckoutItemDto item = new CheckoutItemDto();
        item.setItemType(PurchaseItemType.EVENTO); item.setReferenceId(occurrenceId); item.setQuantity((short) 2);
        CheckoutRequestDto request = new CheckoutRequestDto(); request.setItems(List.of(item)); return request;
    }

    UserDetailsImpl professional(Long id) { return principal(id, "ROLE_PROFESSIONAL"); }
    UserDetailsImpl principal(Long id, String role) {
        return new UserDetailsImpl(id, "test", "test@example.com", "", true, List.of(new SimpleGrantedAuthority(role)));
    }

    @Test void professionalListsOnlyOwnBookingsAndFilters() throws Exception {
        for (int i = 2; i <= 4; i++) {
            var other = BookingFixtures.create(em, "other" + i);
            payments.createOrder(other.client().getId(), checkout(other.occurrence().getId()));
            mvc.perform(get(BASE).with(user(professional(other.professional().getId()))))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.content[0].eventId").value(other.occurrence().getEvent().getId()));
            mvc.perform(get(BASE + "/" + code).with(user(professional(other.professional().getId()))))
                    .andExpect(status().isNotFound());
            mvc.perform(patch(BASE + "/" + code + "/status").with(user(professional(other.professional().getId())))
                    .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CONFIRMADA\"}"))
                    .andExpect(status().isNotFound());
        }
        mvc.perform(get(BASE).param("eventId", fixture.occurrence().getEvent().getId().toString())
                        .param("occurrenceId", fixture.occurrence().getId().toString()).param("status", "PENDIENTE")
                        .with(user(professional(fixture.professional().getId()))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].code").value(code));
        mvc.perform(get(BASE).param("status", "CONFIRMADA").with(user(professional(fixture.professional().getId()))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content").isEmpty());
    }

    @Test void unauthenticatedAndClientCannotAccessProfessionalBookings() throws Exception {
        mvc.perform(get(BASE)).andExpect(status().isUnauthorized());
        mvc.perform(get(BASE).with(user(principal(fixture.client().getId(), "ROLE_USER")))).andExpect(status().isForbidden());
        mvc.perform(patch(BASE + "/" + code + "/status").with(user(principal(fixture.client().getId(), "ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CONFIRMADA\"}")).andExpect(status().isForbidden());
    }

    @Test void acceptanceKeepsReservedSpotsAndUpdatesCustomerAndHistory() throws Exception {
        mvc.perform(patch(BASE + "/" + code + "/status").with(user(professional(fixture.professional().getId())))
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CONFIRMADA\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.booking.status").value("CONFIRMADA"))
                .andExpect(jsonPath("$.reservedSpots").value(2)).andExpect(jsonPath("$.availableSpots").value(8))
                .andExpect(jsonPath("$.history[0].changedBy").value(fixture.professional().getUsername()));
        mvc.perform(get("/api/v1/payments/checkout/bookings/me").with(user(principal(fixture.client().getId(), "ROLE_USER"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].status").value("CONFIRMADA"));
        assertThat(orders.findByCode(orderCode).orElseThrow().getPaidAt()).isNull();
        assertThat(service.detail(fixture.professional().getId(), code).attendees()).hasSize(1);
    }

    @Test void rejectionReleasesSpotsExactlyOnceAndPreventsSubsequentPayment() throws Exception {
        mvc.perform(patch(BASE + "/" + code + "/status").with(user(professional(fixture.professional().getId())))
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CANCELADA\",\"reason\":\"Sin disponibilidad\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.reservedSpots").value(0))
                .andExpect(jsonPath("$.history[0].reason").value("Sin disponibilidad"));
        assertThat(bookings.findByCode(code).orElseThrow().getCancelledAt()).isNotNull();
        assertThatThrownBy(() -> service.decide(fixture.professional().getId(), code, new Decision(BookingStatus.CANCELADA, "Otra vez")))
                .isInstanceOf(BookingConflictException.class);
        assertThat(history.findByBookingTypeAndBookingIdOrderByCreatedAtAscIdAsc(PurchaseItemType.EVENTO,
                bookings.findByCode(code).orElseThrow().getId())).hasSize(1);
        mvc.perform(post("/api/v1/payments/checkout/orders/" + orderCode + "/pay")
                .with(user(principal(fixture.client().getId(), "ROLE_USER"))).contentType(MediaType.APPLICATION_JSON)
                .content("{\"method\":\"TARJETA\",\"cardNumber\":\"4111111111111111\",\"cardCvv\":\"123\",\"cardExpiration\":\"12/30\"}"))
                .andExpect(status().isConflict());
        assertThat(fixture.occurrence().getReservedSpots()).isZero();
        var mine = payments.getMyBookings(fixture.client().getId()).getFirst();
        assertThat(mine.getStatus()).isEqualTo(BookingStatus.CANCELADA);
        assertThat(mine.getCancellationCause()).isEqualTo("RECHAZADA");
        assertThat(mine.getCancellationReason()).isEqualTo("Sin disponibilidad");
        assertThat(mine.getCancelledAt()).isNotNull();
    }

    @Test void paidBookingCannotBeManagedAgain() {
        PaymentProcessRequestDto request = new PaymentProcessRequestDto();
        request.setMethod("TARJETA"); request.setCardNumber("4111111111111111");
        request.setCardCvv("123"); request.setCardExpiration("12/30");
        payments.processPayment(orderCode, request);
        assertThatThrownBy(() -> service.decide(fixture.professional().getId(), code, new Decision(BookingStatus.CANCELADA, "Rechazar")))
                .isInstanceOf(BookingConflictException.class);
        assertThat(bookings.findByCode(code).orElseThrow().getStatus()).isEqualTo(BookingStatus.CONFIRMADA);
        assertThat(fixture.occurrence().getReservedSpots()).isEqualTo(2);
    }

    @Test void validatesReasonStatusAndPagination() throws Exception {
        for (String body : List.of("{\"status\":\"CANCELADA\"}", "{\"status\":\"CANCELADA\",\"reason\":\"   \"}",
                "{\"status\":\"COMPLETADA\"}", "{\"status\":\"CANCELADA\",\"reason\":\"" + "x".repeat(256) + "\"}")) {
            mvc.perform(patch(BASE + "/" + code + "/status").with(user(professional(fixture.professional().getId())))
                    .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isBadRequest());
        }
        mvc.perform(get(BASE).param("size", "101").with(user(professional(fixture.professional().getId()))))
                .andExpect(status().isBadRequest());
        mvc.perform(get(BASE + "/UNKNOWN").with(user(professional(fixture.professional().getId()))))
                .andExpect(status().isNotFound());
    }

    @Test void cannotAcceptAnOccurrenceThatIsCancelledOrPast() throws Exception {
        fixture.occurrence().setStatus(EventOccurrenceStatus.CANCELADA);
        mvc.perform(patch(BASE + "/" + code + "/status").with(user(professional(fixture.professional().getId())))
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CONFIRMADA\"}")).andExpect(status().isConflict());
        fixture.occurrence().setStatus(EventOccurrenceStatus.PROGRAMADA);
        fixture.occurrence().setStartsAt(java.time.Instant.now().minusSeconds(10));
        mvc.perform(patch(BASE + "/" + code + "/status").with(user(professional(fixture.professional().getId())))
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CONFIRMADA\"}")).andExpect(status().isConflict());
    }

    @Test void expiredOrderCanOnlyBeRejected() throws Exception {
        orders.findByCode(orderCode).orElseThrow().setExpiresAt(java.time.Instant.now().minusSeconds(10));
        mvc.perform(patch(BASE + "/" + code + "/status").with(user(professional(fixture.professional().getId())))
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CONFIRMADA\"}")).andExpect(status().isConflict());
        assertThat(bookings.findByCode(code).orElseThrow().getStatus()).isEqualTo(BookingStatus.PENDIENTE);
        mvc.perform(patch(BASE + "/" + code + "/status").with(user(professional(fixture.professional().getId())))
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CANCELADA\",\"reason\":\"Orden expirada\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.reservedSpots").value(0));
    }

    @Test void rejectingOneBookingKeepsTheRestOfTheOrderPayable() throws Exception {
        var second = BookingFixtures.create(em, "second");
        CheckoutRequestDto request = checkout(fixture.occurrence().getId());
        request.setItems(List.of(request.getItems().getFirst(), checkout(second.occurrence().getId()).getItems().getFirst()));
        String mixedOrder = payments.createOrder(fixture.client().getId(), request).getCode();
        var mixedBookings = bookings.findLockedByOrderId(orders.findByCode(mixedOrder).orElseThrow().getId());
        String rejected = mixedBookings.get(0).getCode();
        String kept = mixedBookings.get(1).getCode();

        service.decide(fixture.professional().getId(), rejected, new Decision(BookingStatus.CANCELADA, "Sin cupo"));
        var order = orders.findByCode(mixedOrder).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(com.hean.consigueventas.oonabe.payment.domain.PurchaseOrderStatus.ABIERTA);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("50.00");

        PaymentProcessRequestDto pay = new PaymentProcessRequestDto();
        pay.setMethod("TARJETA"); pay.setCardNumber("4111111111111111"); pay.setCardCvv("123"); pay.setCardExpiration("12/30");
        assertThat(payments.processPayment(mixedOrder, pay).getStatus()).isEqualTo("APROBADO");
        assertThat(bookings.findByCode(rejected).orElseThrow().getStatus()).isEqualTo(BookingStatus.CANCELADA);
        assertThat(bookings.findByCode(kept).orElseThrow().getStatus()).isEqualTo(BookingStatus.CONFIRMADA);
    }

    @Test void expiredUnpaidOrderReleasesItsSpotsOnce() throws Exception {
        service.decide(fixture.professional().getId(), code, new Decision(BookingStatus.CONFIRMADA, null));
        assertThat(release.findReleasableOrderCodes()).doesNotContain(orderCode);
        orders.findByCode(orderCode).orElseThrow().setExpiresAt(java.time.Instant.now().minusSeconds(10));
        assertThat(release.findReleasableOrderCodes()).contains(orderCode);

        assertThat(release.release(orderCode)).isEqualTo(1);
        assertThat(release.release(orderCode)).isZero();
        var booking = bookings.findByCode(code).orElseThrow();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELADA);
        assertThat(booking.getCancelledAt()).isNotNull();
        assertThat(fixture.occurrence().getReservedSpots()).isZero();
        assertThat(orders.findByCode(orderCode).orElseThrow().getStatus())
                .isEqualTo(com.hean.consigueventas.oonabe.payment.domain.PurchaseOrderStatus.EXPIRADA);
        assertThat(release.findReleasableOrderCodes()).doesNotContain(orderCode);
        mvc.perform(get(BASE + "/" + code).with(user(professional(fixture.professional().getId()))))
                .andExpect(jsonPath("$.history[1].reason").value("Orden expirada sin pago"))
                .andExpect(jsonPath("$.history[1].changedBy").doesNotExist());
        var mine = payments.getMyBookings(fixture.client().getId()).getFirst();
        assertThat(mine.getStatus()).isEqualTo(BookingStatus.CANCELADA);
        assertThat(mine.getCancellationCause()).isEqualTo("EXPIRADA");
        assertThat(mine.getCancellationReason()).isEqualTo("Orden expirada sin pago");
    }

    @Test void paidOrderIsNeverReleased() {
        PaymentProcessRequestDto request = new PaymentProcessRequestDto();
        request.setMethod("TARJETA"); request.setCardNumber("4111111111111111");
        request.setCardCvv("123"); request.setCardExpiration("12/30");
        payments.processPayment(orderCode, request);
        orders.findByCode(orderCode).orElseThrow().setExpiresAt(java.time.Instant.now().minusSeconds(10));
        assertThat(release.findReleasableOrderCodes()).doesNotContain(orderCode);
        assertThat(release.release(orderCode)).isZero();
        assertThat(fixture.occurrence().getReservedSpots()).isEqualTo(2);
    }

    @Test void swaggerDocumentsTheProfessionalContract() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['" + BASE + "'].get.summary").exists())
                .andExpect(jsonPath("$.paths['" + BASE + "/{code}/status'].patch.responses['409']").exists());
    }
}
