package com.hean.consigueventas.oonabe.booking.config;

import com.hean.consigueventas.oonabe.common.enums.BookingStatus;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class BookingStatusSchemaMigrationTest {

    @Test
    void constraintIncludesEveryBookingStatus() {
        String allowedStatuses = BookingStatusSchemaMigration.allowedStatusesSql();

        assertThat(Arrays.stream(BookingStatus.values()).map(Enum::name))
                .allMatch(allowedStatuses::contains);
        assertThat(BookingStatusSchemaMigration.containsAllStatuses(
                "CHECK (status IN (" + allowedStatuses + "))"))
                .isTrue();
    }

    @Test
    void detectsLegacyConstraintMissingRescheduleProposal() {
        String legacyDefinition = "CHECK (status IN ('PENDIENTE', 'CONFIRMADA', 'CANCELADA'))";

        assertThat(BookingStatusSchemaMigration.containsAllStatuses(legacyDefinition)).isFalse();
    }
}
