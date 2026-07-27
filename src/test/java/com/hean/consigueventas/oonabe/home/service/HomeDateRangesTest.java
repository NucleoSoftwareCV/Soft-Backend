package com.hean.consigueventas.oonabe.home.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class HomeDateRangesTest {

    @Test
    void remainingWeekRunsFromTodayThroughSunday() {
        var range = HomeDateRanges.remainingWeek(LocalDate.of(2026, 7, 27));

        assertThat(range.from()).isEqualTo(LocalDate.of(2026, 7, 27));
        assertThat(range.to()).isEqualTo(LocalDate.of(2026, 8, 2));
    }

    @Test
    void weekendStartsOnNextFridayDuringTheWeek() {
        var range = HomeDateRanges.currentOrNextWeekend(LocalDate.of(2026, 7, 29));

        assertThat(range.from()).isEqualTo(LocalDate.of(2026, 7, 31));
        assertThat(range.to()).isEqualTo(LocalDate.of(2026, 8, 2));
    }

    @Test
    void weekendKeepsOnlyRemainingDaysWhenAlreadyStarted() {
        var saturday = HomeDateRanges.currentOrNextWeekend(LocalDate.of(2026, 8, 1));
        var sunday = HomeDateRanges.currentOrNextWeekend(LocalDate.of(2026, 8, 2));

        assertThat(saturday.from()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(saturday.to()).isEqualTo(LocalDate.of(2026, 8, 2));
        assertThat(sunday.from()).isEqualTo(LocalDate.of(2026, 8, 2));
        assertThat(sunday.to()).isEqualTo(LocalDate.of(2026, 8, 2));
    }
}
