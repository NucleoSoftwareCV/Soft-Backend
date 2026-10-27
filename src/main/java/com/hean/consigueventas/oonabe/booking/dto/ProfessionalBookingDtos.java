package com.hean.consigueventas.oonabe.booking.dto;

import com.hean.consigueventas.oonabe.common.enums.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class ProfessionalBookingDtos {
    private ProfessionalBookingDtos() {}

    @Schema(description = "Reserva de un evento del profesional autenticado")
    public record Summary(String code, String customerName, Long eventId, String eventTitle,
            Long occurrenceId, Instant occurrenceStartsAt, short quantity, BigDecimal totalAmount,
            String currency, BookingStatus status, Instant createdAt) {}

    @Schema(description = "Detalle de reserva, disponibilidad actual y trazabilidad")
    public record Detail(Summary booking, String customerEmail, String customerPhone,
            int capacity, int reservedSpots, int availableSpots,
            List<Attendee> attendees, List<History> history) {}

    public record Attendee(Long id, String name, String email, String attendanceStatus) {}
    public record History(String previousStatus, String newStatus, String changedBy,
            String reason, Instant createdAt) {}

    @Schema(description = "Decisión sobre una reserva pendiente: CONFIRMADA o CANCELADA")
    public record Decision(@NotNull BookingStatus status,
            @Size(max = 255) @Schema(description = "Obligatorio al rechazar", example = "La solicitud no puede ser atendida") String reason) {
        @AssertTrue(message = "Solo se permite confirmar o cancelar una reserva pendiente")
        @Schema(hidden = true)
        public boolean isSupportedStatus() {
            return status == null || status == BookingStatus.CONFIRMADA || status == BookingStatus.CANCELADA;
        }

        @AssertTrue(message = "El motivo es obligatorio al rechazar")
        @Schema(hidden = true)
        public boolean isReasonValid() {
            return status != BookingStatus.CANCELADA || reason != null && !reason.isBlank();
        }
    }
}
