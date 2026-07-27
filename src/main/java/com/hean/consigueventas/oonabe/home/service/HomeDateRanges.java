package com.hean.consigueventas.oonabe.home.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

final class HomeDateRanges {

    private HomeDateRanges() {
    }

    static DateRange remainingWeek(LocalDate today) {
        return new DateRange(today, today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)));
    }

    static DateRange currentOrNextWeekend(LocalDate today) {
        DayOfWeek day = today.getDayOfWeek();
        LocalDate friday;
        if (day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            friday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.FRIDAY));
        } else {
            friday = today.with(TemporalAdjusters.next(DayOfWeek.FRIDAY));
        }
        return new DateRange(today.isAfter(friday) ? today : friday, friday.plusDays(2));
    }

    record DateRange(LocalDate from, LocalDate to) {
    }
}
