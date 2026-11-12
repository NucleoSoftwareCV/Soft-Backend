package com.hean.consigueventas.oonabe.booking;

import com.hean.consigueventas.oonabe.auth.security.UserDetailsImpl;
import com.hean.consigueventas.oonabe.booking.dto.ProfessionalBookingDtos.Decision;
import com.hean.consigueventas.oonabe.booking.entity.EventBooking;
import com.hean.consigueventas.oonabe.booking.exception.BookingConflictException;
import com.hean.consigueventas.oonabe.booking.repository.EventBookingRepository;
import com.hean.consigueventas.oonabe.booking.service.ClientBookingService;
import com.hean.consigueventas.oonabe.booking.service.ExpiredOrderReleaseService;
import com.hean.consigueventas.oonabe.booking.service.ProfessionalBookingService;
import com.hean.consigueventas.oonabe.common.enums.BookingStatus;
import com.hean.consigueventas.oonabe.common.exception.BusinessLogicException;
import com.hean.consigueventas.oonabe.notification.domain.NotificationType;
import com.hean.consigueventas.oonabe.notification.dto.NotificationDtos.Item;
import com.hean.consigueventas.oonabe.notification.service.NotificationService;
import com.hean.consigueventas.oonabe.payment.domain.PurchaseOrderStatus;
import com.hean.consigueventas.oonabe.payment.dto.PaymentProcessRequestDto;
import com.hean.consigueventas.oonabe.payment.repository.PurchaseOrderRepository;
import com.hean.consigueventas.oonabe.payment.service.PaymentService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UnifiedBookingFlowIntegrationTest {
    static final String MY_BOOKINGS = "/api/v1/payments/checkout/bookings/me";
    static final String NOTIFICATIONS = "/api/v1/notifications";

    @Autowired EntityManager em;
    @Autowired MockMvc mvc;
    @Autowired PaymentService payments;
    @Autowired ProfessionalBookingService professionalBookings;
    @Autowired ClientBookingService clientBookings;
    @Autowired ExpiredOrderReleaseService release;
    @Autowired NotificationService notifications;
    @Autowired EventBookingRepository bookings;
    @Autowired PurchaseOrderRepository orders;
    BookingFixtures.Fixture fixture;

    @BeforeEach void setup() {
        fixture = BookingFixtures.create(em, "unified");
    }

    private String request() {
        return payments.createOrder(fixture.client().getId(),
                ProfessionalBookingIntegrationTest.checkout(fixture.occurrence().getId())).getCode();
    }

    private EventBooking bookingOf(String orderCode) {
        return bookings.findLockedByOrderId(orders.findByCode(orderCode).orElseThrow().getId()).getFirst();
    }

    private void accept(EventBooking booking) {
        professionalBookings.decide(fixture.professional().getId(), booking.getCode(), new Decision(BookingStatus.ACEPTADA, null));
    }

    private static PaymentProcessRequestDto card(String number) {
        PaymentProcessRequestDto request = new PaymentProcessRequestDto();
        request.setMethod("TARJETA"); request.setCardNumber(number);
        request.setCardCvv("123"); request.setCardExpiration("12/30");
        return request;
    }

    private static PaymentProcessRequestDto yape() {
        PaymentProcessRequestDto request = new PaymentProcessRequestDto();
        request.setMethod("YAPE"); request.setYapePhoneNumber("987654321"); request.setYapeOtp("123456");
        return request;
    }

    private UserDetailsImpl client() {
        return principal(fixture.client().getId(), "ROLE_USER");
    }

    private static UserDetailsImpl principal(Long id, String role) {
        return new UserDetailsImpl(id, "test", "test@example.com", "", true, List.of(new SimpleGrantedAuthority(role)));
    }

    private List<NotificationType> typesFor(Long userId) {
        return notifications.list(userId, 0, 50).content().stream().map(Item::type).toList();
    }

    // --- Solicitud, aceptación y pago ---------------------------------------------------------

    @Test void paymentRequiresTheProfessionalsAcceptanceAndConfirmsTheBooking() {
        String orderCode = request();
        EventBooking booking = bookingOf(orderCode);
        assertThat(booking.getExpiresAt()).isEqualTo(fixture.occurrence().getStartsAt());

        assertThatThrownBy(() -> payments.processPayment(orderCode, card("4111111111111111")))
                .isInstanceOf(BookingConflictException.class);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDIENTE);

        accept(booking);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.ACEPTADA);
        assertThat(orders.findByCode(orderCode).orElseThrow().getPaidAt()).isNull();
        var mine = payments.getMyBookings(fixture.client().getId()).getFirst();
        assertThat(mine.getOrderCode()).isEqualTo(orderCode);
        assertThat(mine.getOccurrenceId()).isEqualTo(fixture.occurrence().getId());
        assertThat(mine.getPaymentDeadline()).isCloseTo(fixture.occurrence().getStartsAt(), within(1, java.time.temporal.ChronoUnit.HOURS));

        assertThat(payments.processPayment(orderCode, yape()).getStatus()).isEqualTo("APROBADO");
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMADA);
        assertThat(orders.findByCode(orderCode).orElseThrow().getStatus()).isEqualTo(PurchaseOrderStatus.PAGADA);
        assertThat(payments.getMyBookings(fixture.client().getId()).getFirst().getPaymentDeadline()).isNull();
    }

    @Test void paymentWindowIsTwentyFourHoursButNeverPassesTheEventStart() {
        fixture.occurrence().setStartsAt(Instant.now().plus(Duration.ofDays(5)));
        fixture.occurrence().setEndsAt(Instant.now().plus(Duration.ofDays(6)));
        EventBooking booking = bookingOf(request());
        accept(booking);
        assertThat(booking.getExpiresAt()).isCloseTo(Instant.now().plus(Duration.ofHours(24)),
                within(1, java.time.temporal.ChronoUnit.MINUTES));

        var other = BookingFixtures.create(em, "soon");
        other.occurrence().setStartsAt(Instant.now().plus(Duration.ofHours(2)));
        EventBooking soon = bookingOf(payments.createOrder(other.client().getId(),
                ProfessionalBookingIntegrationTest.checkout(other.occurrence().getId())).getCode());
        professionalBookings.decide(other.professional().getId(), soon.getCode(), new Decision(BookingStatus.ACEPTADA, null));
        assertThat(soon.getExpiresAt()).isEqualTo(other.occurrence().getStartsAt());
    }

    @Test void declinedPaymentCanBeRetriedOnTheSameOrder() {
        String orderCode = request();
        accept(bookingOf(orderCode));
        assertThat(payments.processPayment(orderCode, card("4111111111114444")).getStatus()).isEqualTo("RECHAZADO");
        assertThat(bookingOf(orderCode).getStatus()).isEqualTo(BookingStatus.ACEPTADA);
        assertThat(payments.processPayment(orderCode, card("4111111111111111")).getStatus()).isEqualTo("APROBADO");
    }

    @Test void whatsAppIsNoLongerAPaymentMethod() {
        String orderCode = request();
        accept(bookingOf(orderCode));
        PaymentProcessRequestDto whatsApp = new PaymentProcessRequestDto();
        whatsApp.setMethod("WHATSAPP");
        assertThatThrownBy(() -> payments.processPayment(orderCode, whatsApp)).isInstanceOf(BusinessLogicException.class);
    }

    @Test void freeBookingIsConfirmedByTheAcceptance() {
        fixture.occurrence().getEvent().setPriceFrom(BigDecimal.ZERO);
        String orderCode = request();
        EventBooking booking = bookingOf(orderCode);
        accept(booking);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMADA);
        assertThat(booking.getExpiresAt()).isNull();
        assertThat(orders.findByCode(orderCode).orElseThrow().getStatus()).isEqualTo(PurchaseOrderStatus.PAGADA);
        assertThat(typesFor(fixture.client().getId())).contains(NotificationType.RESERVA_GRATUITA_CONFIRMADA)
                .doesNotContain(NotificationType.RESERVA_ACEPTADA);
    }

    // --- Duplicados ---------------------------------------------------------------------------

    @Test void onlyOneActiveBookingPerDateUntilItIsCancelled() {
        EventBooking first = bookingOf(request());
        assertThatThrownBy(this::request).isInstanceOf(BookingConflictException.class);
        assertThat(fixture.occurrence().getReservedSpots()).isEqualTo(2);

        clientBookings.cancel(fixture.client().getId(), first.getCode());
        assertThat(bookingOf(request()).getStatus()).isEqualTo(BookingStatus.PENDIENTE);
    }

    // --- Cancelación del cliente --------------------------------------------------------------

    @Test void clientCancelsBeforePayingAndFreesTheSpots() throws Exception {
        String pendingOrder = request();
        EventBooking pending = bookingOf(pendingOrder);
        mvc.perform(post(MY_BOOKINGS + "/" + pending.getCode() + "/cancel").with(user(client())))
                .andExpect(status().isNoContent());
        assertThat(pending.getStatus()).isEqualTo(BookingStatus.CANCELADA);
        assertThat(fixture.occurrence().getReservedSpots()).isZero();
        assertThat(orders.findByCode(pendingOrder).orElseThrow().getStatus()).isEqualTo(PurchaseOrderStatus.CANCELADA);
        var mine = payments.getMyBookings(fixture.client().getId()).getFirst();
        assertThat(mine.getCancellationCause()).isEqualTo("CLIENTE");

        EventBooking accepted = bookingOf(request());
        accept(accepted);
        mvc.perform(post(MY_BOOKINGS + "/" + accepted.getCode() + "/cancel").with(user(client())))
                .andExpect(status().isNoContent());
        assertThat(accepted.getStatus()).isEqualTo(BookingStatus.CANCELADA);
        assertThat(fixture.occurrence().getReservedSpots()).isZero();
    }

    @Test void paidBookingCannotBeCancelledAndOthersCannotTouchIt() throws Exception {
        String orderCode = request();
        EventBooking booking = bookingOf(orderCode);
        String path = MY_BOOKINGS + "/" + booking.getCode() + "/cancel";

        mvc.perform(post(path)).andExpect(status().isUnauthorized());
        mvc.perform(post(path).with(user(principal(fixture.professional().getId(), "ROLE_USER"))))
                .andExpect(status().isNotFound());

        accept(booking);
        payments.processPayment(orderCode, card("4111111111111111"));
        mvc.perform(post(path).with(user(client()))).andExpect(status().isConflict());
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMADA);
        assertThat(fixture.occurrence().getReservedSpots()).isEqualTo(2);
    }

    // --- Vencimiento --------------------------------------------------------------------------

    @Test void acceptedBookingExpiresWithoutPaymentAndFreesTheSpots() {
        String orderCode = request();
        EventBooking booking = bookingOf(orderCode);
        accept(booking);
        assertThat(release.findReleasableBookingCodes()).doesNotContain(booking.getCode());

        booking.setExpiresAt(Instant.now().minusSeconds(10));
        em.flush();
        assertThatThrownBy(() -> payments.processPayment(orderCode, card("4111111111111111")))
                .isInstanceOf(BookingConflictException.class);
        assertThat(release.findReleasableBookingCodes()).contains(booking.getCode());

        assertThat(release.release(booking.getCode())).isTrue();
        assertThat(release.release(booking.getCode())).isFalse();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELADA);
        assertThat(fixture.occurrence().getReservedSpots()).isZero();
        assertThat(orders.findByCode(orderCode).orElseThrow().getStatus()).isEqualTo(PurchaseOrderStatus.EXPIRADA);
        assertThat(payments.getMyBookings(fixture.client().getId()).getFirst().getCancellationCause()).isEqualTo("EXPIRADA");
        assertThat(typesFor(fixture.client().getId())).contains(NotificationType.RESERVA_VENCIDA);
        assertThat(typesFor(fixture.professional().getId())).contains(NotificationType.RESERVA_EXPIRADA);
    }

    @Test void unansweredRequestIsReleasedWhenTheEventStarts() {
        EventBooking booking = bookingOf(request());
        assertThat(release.findReleasableBookingCodes()).doesNotContain(booking.getCode());

        booking.setExpiresAt(Instant.now().minusSeconds(10));
        em.flush();
        assertThat(release.release(booking.getCode())).isTrue();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELADA);
        assertThat(fixture.occurrence().getReservedSpots()).isZero();
    }

    @Test void confirmedBookingIsNeverReleased() {
        String orderCode = request();
        EventBooking booking = bookingOf(orderCode);
        accept(booking);
        payments.processPayment(orderCode, card("4111111111111111"));
        fixture.occurrence().setStartsAt(Instant.now().minusSeconds(10));
        em.flush();

        assertThat(release.findReleasableBookingCodes()).doesNotContain(booking.getCode());
        assertThat(release.release(booking.getCode())).isFalse();
        assertThat(fixture.occurrence().getReservedSpots()).isEqualTo(2);
    }

    // --- Notificaciones -----------------------------------------------------------------------

    @Test void everyTransitionNotifiesCustomerAndProfessional() {
        String orderCode = request();
        accept(bookingOf(orderCode));
        payments.processPayment(orderCode, card("4111111111111111"));

        assertThat(typesFor(fixture.client().getId())).containsExactlyInAnyOrder(NotificationType.SOLICITUD_ENVIADA,
                NotificationType.RESERVA_ACEPTADA, NotificationType.RESERVA_CONFIRMADA);
        assertThat(typesFor(fixture.professional().getId())).containsExactlyInAnyOrder(NotificationType.NUEVA_SOLICITUD,
                NotificationType.PAGO_COMPLETADO);
    }

    @Test void rejectionAndCancellationNotifyTheRightPerson() {
        EventBooking rejected = bookingOf(request());
        professionalBookings.decide(fixture.professional().getId(), rejected.getCode(), new Decision(BookingStatus.CANCELADA, "Sin plazas"));
        EventBooking cancelled = bookingOf(request());
        clientBookings.cancel(fixture.client().getId(), cancelled.getCode());

        assertThat(typesFor(fixture.client().getId())).contains(NotificationType.RESERVA_RECHAZADA, NotificationType.SOLICITUD_CANCELADA);
        assertThat(typesFor(fixture.professional().getId())).contains(NotificationType.CANCELACION_CLIENTE);
    }

    @Test void inboxLinksToTheBookingAndTracksWhatWasRead() throws Exception {
        EventBooking booking = bookingOf(request());
        accept(booking);

        mvc.perform(get(NOTIFICATIONS + "/me")).andExpect(status().isUnauthorized());
        var page = notifications.list(fixture.client().getId(), 0, 10).content();
        Item accepted = page.getFirst();
        assertThat(accepted.type()).isEqualTo(NotificationType.RESERVA_ACEPTADA);
        mvc.perform(get(NOTIFICATIONS + "/me").with(user(client())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].title").value("¡Tu reserva fue aceptada!"))
                .andExpect(jsonPath("$.content[0].actionLabel").value("Realizar pago"))
                .andExpect(jsonPath("$.content[0].bookingCode").value(booking.getCode()))
                .andExpect(jsonPath("$.content[0].eventTitle").value("Evento unified"))
                .andExpect(jsonPath("$.content[0].read").value(false));
        mvc.perform(get(NOTIFICATIONS + "/me/unread-count").with(user(client())))
                .andExpect(jsonPath("$.count").value(2));

        mvc.perform(patch(NOTIFICATIONS + "/" + accepted.id() + "/read").with(user(principal(fixture.professional().getId(), "ROLE_USER"))))
                .andExpect(status().isNotFound());
        mvc.perform(patch(NOTIFICATIONS + "/" + accepted.id() + "/read").with(user(client())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.read").value(true));
        mvc.perform(get(NOTIFICATIONS + "/me/unread-count").with(user(client())))
                .andExpect(jsonPath("$.count").value(1));
        mvc.perform(patch(NOTIFICATIONS + "/me/read-all").with(user(client()))).andExpect(status().isNoContent());
        mvc.perform(get(NOTIFICATIONS + "/me/unread-count").with(user(client())))
                .andExpect(jsonPath("$.count").value(0));
    }

    // --- Supervisión del administrador --------------------------------------------------------

    @Test void adminSupervisesBookingsWithPaymentAndCancellationActivity() throws Exception {
        String orderCode = request();
        EventBooking paid = bookingOf(orderCode);
        accept(paid);
        payments.processPayment(orderCode, yape());
        var other = BookingFixtures.create(em, "admin-view");
        EventBooking cancelled = bookingOf(payments.createOrder(other.client().getId(),
                ProfessionalBookingIntegrationTest.checkout(other.occurrence().getId())).getCode());
        clientBookings.cancel(other.client().getId(), cancelled.getCode());
        em.flush();
        var admin = user(principal(fixture.professional().getId(), "ROLE_ADMIN"));

        mvc.perform(get("/api/v1/admin/bookings").param("status", "CONFIRMADA").with(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].code").value(paid.getCode()))
                .andExpect(jsonPath("$.content[0].orderStatus").value("PAGADA"))
                .andExpect(jsonPath("$.content[0].paymentMethod").value("YAPE"))
                .andExpect(jsonPath("$.content[0].paidAt").exists())
                .andExpect(jsonPath("$.content[0].professionalName").value("Profesional unified"));
        mvc.perform(get("/api/v1/admin/bookings").param("status", "CANCELADA").with(admin))
                .andExpect(jsonPath("$.content[0].code").value(cancelled.getCode()))
                .andExpect(jsonPath("$.content[0].cancellationCause").value("CLIENTE"));
        mvc.perform(get("/api/v1/admin/bookings").param("from", Instant.now().plusSeconds(60).toString()).with(admin))
                .andExpect(jsonPath("$.content").isEmpty());

        mvc.perform(get("/api/v1/admin/bookings")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/admin/bookings").with(user(client()))).andExpect(status().isForbidden());
    }
}
