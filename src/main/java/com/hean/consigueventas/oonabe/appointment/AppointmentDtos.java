package com.hean.consigueventas.oonabe.appointment;

import com.hean.consigueventas.oonabe.common.enums.SessionModality;
import com.hean.consigueventas.oonabe.common.enums.BookingStatus;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import java.util.List;

public final class AppointmentDtos {
    private AppointmentDtos() {}
    public record Venue(Long id, String name, String address) {}
    public record Opening(int dayOfWeek, LocalTime startTime, LocalTime endTime,
                          LocalDate validFrom, LocalDate validUntil, Long locationId) {}
    public record Options(Long serviceId, String title, String professionalName, int durationMinutes,
                          BigDecimal price, String currency, List<SessionModality> modalities,
                          List<Venue> venues, String timeZone, LocalDate minDate, LocalDate maxDate, List<Opening> openings) {}
    public record Slot(Instant startsAt, Instant endsAt) {}
    public record Availability(LocalDate date, String timeZone, List<Slot> slots) {}
    public record Request(@NotNull @Positive Long serviceId, @NotNull SessionModality modality,
                          Long locationId, @NotNull Instant startsAt, @NotBlank @Size(max=2000) String reason) {}
    public record Receipt(Long id, String code, Long serviceId, String title, String professionalName,
                          String customerName, Instant startsAt, Instant endsAt, SessionModality modality,
                          Venue venue, BigDecimal price, String currency, String status, Instant expiresAt,
                          String reason, Instant cancelledAt, String cancellationReason, String cancelledBy,
                          Instant rescheduledAt, Instant previousStartsAt, Instant previousEndsAt,
                          String rescheduleReason) {}
    public record RuleInput(@Min(1) @Max(7) int dayOfWeek, @NotNull LocalTime startTime,
                            @NotNull LocalTime endTime, @NotNull LocalDate validFrom, LocalDate validUntil,
                            Long locationId) {}
    public record Rule(Long id, int dayOfWeek, LocalTime startTime, LocalTime endTime,
                       LocalDate validFrom, LocalDate validUntil, Venue venue) {}
    public record Decision(@NotNull BookingStatus status, @Size(max=500) String reason) {}
    public record Cancellation(@NotBlank @Size(max=500) String reason) {}
    public record Reschedule(@NotNull SessionModality modality, Long locationId,
                             @NotNull Instant startsAt, @NotBlank @Size(max=500) String reason) {}
}
