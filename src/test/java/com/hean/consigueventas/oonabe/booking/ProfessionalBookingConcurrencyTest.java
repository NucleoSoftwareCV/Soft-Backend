package com.hean.consigueventas.oonabe.booking;

import com.hean.consigueventas.oonabe.booking.dto.ProfessionalBookingDtos.Decision;
import com.hean.consigueventas.oonabe.booking.exception.BookingConflictException;
import com.hean.consigueventas.oonabe.booking.repository.*;
import com.hean.consigueventas.oonabe.booking.service.ProfessionalBookingService;
import com.hean.consigueventas.oonabe.common.enums.*;
import com.hean.consigueventas.oonabe.common.exception.BusinessLogicException;
import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import com.hean.consigueventas.oonabe.payment.dto.PaymentProcessRequestDto;
import com.hean.consigueventas.oonabe.payment.service.PaymentService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Supplier;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.mail.host=")
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class ProfessionalBookingConcurrencyTest {
    @Container static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");
    @DynamicPropertySource static void database(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        r.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        r.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        r.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }
    @Autowired TransactionTemplate tx;
    @Autowired EntityManager em;
    @Autowired PaymentService payments;
    @Autowired ProfessionalBookingService service;
    @Autowired EventBookingRepository bookings;
    @Autowired BookingStatusHistoryRepository history;
    Long professionalId;
    Long clientId;
    Long occurrenceId;

    @BeforeEach void fixture() {
        tx.executeWithoutResult(status -> {
            var f = BookingFixtures.create(em, UUID.randomUUID().toString().substring(0, 8));
            professionalId = f.professional().getId(); clientId = f.client().getId(); occurrenceId = f.occurrence().getId();
        });
    }

    private String reserve() {
        payments.createOrder(clientId, ProfessionalBookingIntegrationTest.checkout(occurrenceId));
        return payments.getMyBookings(clientId).getFirst().getCode();
    }

    @Test void twoDecisionsProduceOnlyOneTransitionAndOneCapacityEffect() throws Exception {
        String code = reserve();
        var outcomes = race(
                () -> { service.decide(professionalId, code, new Decision(BookingStatus.CONFIRMADA, null)); return true; },
                () -> { service.decide(professionalId, code, new Decision(BookingStatus.CANCELADA, "No disponible")); return true; });
        assertThat(outcomes).containsExactlyInAnyOrder(true, false);
        tx.executeWithoutResult(status -> {
            var b = bookings.findByCode(code).orElseThrow();
            assertThat(history.findByBookingTypeAndBookingIdOrderByCreatedAtAscIdAsc(PurchaseItemType.EVENTO, b.getId())).hasSize(1);
            assertThat(em.find(EventOccurrence.class, occurrenceId).getReservedSpots())
                    .isEqualTo(b.getStatus() == BookingStatus.CANCELADA ? 0 : 2);
        });
    }

    @Test void checkoutDoesNotOversellTheLastSeats() throws Exception {
        tx.executeWithoutResult(status -> em.find(EventOccurrence.class, occurrenceId).setCapacity(2));
        var outcomes = race(
                () -> { payments.createOrder(clientId, ProfessionalBookingIntegrationTest.checkout(occurrenceId)); return true; },
                () -> { payments.createOrder(clientId, ProfessionalBookingIntegrationTest.checkout(occurrenceId)); return true; });
        assertThat(outcomes).containsExactlyInAnyOrder(true, false);
        tx.executeWithoutResult(status -> assertThat(em.find(EventOccurrence.class, occurrenceId).getReservedSpots()).isEqualTo(2));
        assertThat(payments.getMyBookings(clientId)).hasSize(1);
    }

    @Test void paymentAndRejectionCannotBothSucceed() throws Exception {
        String code = reserve();
        String orderCode = tx.execute(status -> bookings.findByCode(code).orElseThrow().getOrderItem().getOrder().getCode());
        var outcomes = race(() -> {
            PaymentProcessRequestDto request = new PaymentProcessRequestDto();
            request.setMethod("TARJETA"); request.setCardNumber("4111111111111111");
            request.setCardExpiration("12/30"); request.setCardCvv("123");
            payments.processPayment(orderCode, request); return true;
        }, () -> { service.decide(professionalId, code, new Decision(BookingStatus.CANCELADA, "No disponible")); return true; });
        assertThat(outcomes).containsExactlyInAnyOrder(true, false);
        tx.executeWithoutResult(status -> {
            var b = bookings.findByCode(code).orElseThrow();
            boolean paid = b.getOrderItem().getOrder().getPaidAt() != null;
            assertThat(b.getStatus()).isEqualTo(paid ? BookingStatus.CONFIRMADA : BookingStatus.CANCELADA);
            assertThat(b.getOccurrence().getReservedSpots()).isEqualTo(paid ? 2 : 0);
        });
    }

    private List<Boolean> race(Supplier<Boolean> first, Supplier<Boolean> second) throws Exception {
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var futures = List.of(first, second).stream().map(action -> executor.submit(() -> {
                ready.countDown();
                if (!start.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("No se inició la carrera");
                try { return action.get(); }
                catch (BookingConflictException | BusinessLogicException expected) { return false; }
            })).toList();
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            return List.of(futures.get(0).get(20, TimeUnit.SECONDS), futures.get(1).get(20, TimeUnit.SECONDS));
        }
    }
}
