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
                          LocalDate validFrom, LocalDate validUntil, Long locationId,
                          int breakMinutes, String virtualUrl) {}
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
                          String rescheduleReason, String rescheduledBy, String virtualUrl) {}
    public record RuleInput(@Min(1) @Max(7) int dayOfWeek, @NotNull LocalTime startTime,
                            @NotNull LocalTime endTime, @NotNull LocalDate validFrom, LocalDate validUntil,
                            Long locationId, @Min(0) @Max(240) int breakMinutes,
                            @Size(max=1000) String virtualUrl) {
        public RuleInput(int dayOfWeek, LocalTime startTime, LocalTime endTime,
                         LocalDate validFrom, LocalDate validUntil, Long locationId) {
            this(dayOfWeek, startTime, endTime, validFrom, validUntil, locationId, 0, null);
        }
    }
    public record Rule(Long id, int dayOfWeek, LocalTime startTime, LocalTime endTime,
                       LocalDate validFrom, LocalDate validUntil, Venue venue,
                       int breakMinutes, String virtualUrl) {}
    public record Decision(@NotNull BookingStatus status, @Size(max=500) String reason) {}
    public record Cancellation(@NotBlank @Size(max=500) String reason) {}
    public record Reschedule(@NotNull SessionModality modality, Long locationId,
                             @NotNull Instant startsAt, @NotBlank @Size(max=500) String reason) {}
    public record ProposalDecision(boolean accepted, @Size(max=500) String reason) {}
    public record BlockInput(@NotNull LocalDateTime startsAt, @NotNull LocalDateTime endsAt,
                             @NotBlank @Size(max=500) String reason) {}
    public record Block(Long id, Instant startsAt, Instant endsAt, String reason) {}
    public record Summary(long total, long pending, long confirmed, long completed,
                          long proposed, Receipt nextAppointment) {}
    public record ContactInput(@NotBlank @Size(max=200) String displayName,
                               @Email @Size(max=150) String email,
                               @Size(max=25) String phone,
                               @Size(max=1000) String notes) {}
    public record Contact(String key, String source, Long id, Long customerId, String displayName,
                          String email, String phone, String notes, long appointmentCount,
                          Instant lastAppointment, Instant nextAppointment) {}
    public record ContactDetail(Contact contact, List<Receipt> history) {}
    public record ContactAppointmentInput(@NotNull @Positive Long serviceId,
                                          @NotNull SessionModality modality,
                                          Long locationId,
                                          @NotNull Instant startsAt,
                                          @NotBlank @Size(max=2000) String reason) {}
}
