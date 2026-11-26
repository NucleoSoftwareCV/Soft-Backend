package com.hean.consigueventas.oonabe.support.dto;

import com.hean.consigueventas.oonabe.support.enums.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public final class SupportDtos {
    private SupportDtos() {}

    public record TicketInput(
            @NotNull TicketCategory category,
            @NotBlank @Size(max = 150) String subject,
            @NotBlank @Size(max = 10000) String description,
            TicketPriority priority,
            Long bookingId,
            Long eventId,
            Long transactionId,
            @Size(max = 100) String complaintReference,
            Long sessionBookingId) {}

    public record ResponseInput(
            @NotBlank @Size(max = 10000) String message, boolean internal, boolean requestInformation) {}

    public record StatusInput(
            @NotNull TicketStatus status, @NotBlank @Size(max = 1000) String reason) {}

    public record PriorityInput(
            @NotNull TicketPriority priority, @NotBlank @Size(max = 1000) String reason) {}

    public record RefundInput(
            @NotNull Long transactionId,
            @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal requestedAmount,
            @NotBlank @Size(max = 100) String reason,
            @NotBlank @Size(max = 10000) String description,
            @Positive Long ticketId) {}

    public record DecisionInput(
            @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal approvedAmount,
            @NotBlank @Size(max = 1000) String reason) {}

    public record ApplicationInput(@NotBlank @Size(max = 1000) String reason) {}
}
