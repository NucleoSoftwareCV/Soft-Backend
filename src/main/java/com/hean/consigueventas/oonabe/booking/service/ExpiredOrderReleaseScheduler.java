package com.hean.consigueventas.oonabe.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Periodically frees the spots of expired unpaid orders. Disabled in tests, which call the service directly. */
@Component
@Profile("!test")
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ExpiredOrderReleaseScheduler {
    private final ExpiredOrderReleaseService service;

    @Scheduled(initialDelayString = "${app.bookings.expired-release.initial-delay:PT30S}",
            fixedDelayString = "${app.bookings.expired-release.delay:PT1M}")
    public void releaseExpiredOrders() {
        for (String orderCode : service.findReleasableOrderCodes()) {
            try {
                int released = service.release(orderCode);
                if (released > 0) log.info("Orden {} expirada: {} reserva(s) liberadas", orderCode, released);
            } catch (RuntimeException ex) {
                // One failing order must not stop the rest; it is retried on the next run.
                log.warn("No se pudo liberar la orden expirada {}", orderCode, ex);
            }
        }
    }
}
