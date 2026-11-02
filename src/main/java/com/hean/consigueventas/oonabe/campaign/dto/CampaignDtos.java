package com.hean.consigueventas.oonabe.campaign.dto;

import com.hean.consigueventas.oonabe.campaign.domain.CampaignEntryStatus;
import com.hean.consigueventas.oonabe.campaign.domain.CampaignPhase;
import com.hean.consigueventas.oonabe.campaign.domain.CampaignStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class CampaignDtos {

    private CampaignDtos() {
    }

    public record Faq(
            @NotBlank @Size(max = 200) String question,
            @NotBlank String answer
    ) {
    }

    @Schema(description = "Resumen de una campaña visible (banner de Explorar)")
    public record Summary(
            String slug,
            String title,
            String subtitle,
            String badgeLabel,
            String heroImageUrl,
            BigDecimal prizeAmount,
            String prizeCurrency,
            Instant participationStartsAt,
            Instant participationEndsAt,
            Instant drawAt,
            @Schema(description = "Fase calculada: PROXIMA, ABIERTA o CERRADA") CampaignPhase phase
    ) {
    }

    @Schema(description = "Detalle público de una campaña")
    public record Detail(
            Summary summary,
            String description,
            String prizeDescription,
            Instant prizeActivationDeadline,
            String participationConditions,
            String legalTerms,
            Integer maxEntriesPerCustomer,
            List<Faq> faqs,
            @Schema(description = "Experiencias incluidas en la campaña") long eligibleEventCount
    ) {
    }

    @Schema(description = "Datos para crear o actualizar una campaña")
    public record UpsertRequest(
            @NotBlank @Size(max = 80) @Pattern(regexp = "[a-z0-9]+(-[a-z0-9]+)*", message = "El slug solo admite minúsculas, números y guiones")
            String slug,
            @NotBlank @Size(max = 180) String title,
            @Size(max = 300) String subtitle,
            @NotBlank String description,
            @Size(max = 60) String badgeLabel,
            @Size(max = 500) String heroImageUrl,
            @NotNull @DecimalMin(value = "0.01", message = "El premio debe ser mayor que cero") BigDecimal prizeAmount,
            @Pattern(regexp = "[A-Z]{3}", message = "La moneda debe ser un código ISO de 3 letras") String prizeCurrency,
            String prizeDescription,
            @NotNull Instant participationStartsAt,
            @NotNull Instant participationEndsAt,
            Instant drawAt,
            Instant prizeActivationDeadline,
            String participationConditions,
            String legalTerms,
            @Min(value = 1, message = "El límite por cliente debe ser al menos 1") Integer maxEntriesPerCustomer,
            List<@Valid Faq> faqs
    ) {
        @AssertTrue(message = "El cierre de la participación debe ser posterior al inicio")
        @Schema(hidden = true)
        public boolean isParticipationWindowValid() {
            return participationStartsAt == null || participationEndsAt == null
                    || participationEndsAt.isAfter(participationStartsAt);
        }

        @AssertTrue(message = "La fecha del sorteo no puede ser anterior al cierre de la participación")
        @Schema(hidden = true)
        public boolean isDrawDateValid() {
            return drawAt == null || participationEndsAt == null || !drawAt.isBefore(participationEndsAt);
        }
    }

    public record StatusRequest(@NotNull CampaignStatus status) {
    }

    public record EventsRequest(@NotNull List<@NotNull Long> eventIds) {
    }

    @Schema(description = "Campaña vista desde administración")
    public record AdminResponse(
            Long id,
            CampaignStatus status,
            Detail detail,
            List<Long> eventIds,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    @Schema(description = "Participación registrada (administración)")
    public record AdminEntry(
            String code,
            String bookingCode,
            String customerName,
            String customerEmail,
            Long eventId,
            String eventTitle,
            int ticketCount,
            CampaignEntryStatus status,
            Instant createdAt,
            Instant voidedAt,
            String voidReason
    ) {
    }

    @Schema(description = "Participación propia del cliente")
    public record MyEntry(
            String code,
            String bookingCode,
            Long eventId,
            String eventTitle,
            Instant occurrenceStartsAt,
            int ticketCount,
            CampaignEntryStatus status,
            Instant createdAt
    ) {
    }

    @Schema(description = "Participaciones del cliente autenticado en una campaña")
    public record MyEntries(
            String campaignSlug,
            @Schema(description = "Suma de plazas de las participaciones válidas") long validTickets,
            List<MyEntry> entries
    ) {
    }

    @Schema(description = "Participación generada en esta operación")
    public record EntrySummary(
            String campaignSlug,
            String campaignTitle,
            String bookingCode,
            int ticketCount
    ) {
    }
}
