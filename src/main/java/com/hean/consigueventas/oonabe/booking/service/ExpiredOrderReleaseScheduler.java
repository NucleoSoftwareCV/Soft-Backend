package com.hean.consigueventas.oonabe.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Periodically frees the spots of expired bookings. Disabled in tests, which call the service directly. */
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
        for (String bookingCode : service.findReleasableBookingCodes()) {
            try {
                if (service.release(bookingCode)) log.info("Reserva {} vencida: plaza liberada", bookingCode);
            } catch (RuntimeException ex) {
                // One failing booking must not stop the rest; it is retried on the next run.
                log.warn("No se pudo liberar la reserva vencida {}", bookingCode, ex);
            }
        }
    }
}
