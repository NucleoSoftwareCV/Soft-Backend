package com.hean.consigueventas.oonabe.event.controller;

import com.hean.consigueventas.oonabe.common.config.OpenApiConfig;
import com.hean.consigueventas.oonabe.common.config.TimeConfig;
import com.hean.consigueventas.oonabe.event.dto.response.EventOccurrenceAdminResponse;
import com.hean.consigueventas.oonabe.event.dto.response.EventOccurrenceCalendarResponse;
import com.hean.consigueventas.oonabe.event.dto.response.EventOccurrenceResponse;
import com.hean.consigueventas.oonabe.event.dto.request.EventOccurrenceRequest;
import com.hean.consigueventas.oonabe.event.dto.request.EventOccurrenceStatusUpdateRequest;
import com.hean.consigueventas.oonabe.event.service.EventOccurrenceService;
import com.hean.consigueventas.oonabe.event.service.EventService;
import com.hean.consigueventas.oonabe.auth.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/event-occurrences")
@Tag(name = "Ocurrencias de eventos", description = "Fechas y horarios programados para los eventos.")
public class EventOccurrenceController {

    private final EventOccurrenceService occurrenceService;
    private final EventService eventService;

    public EventOccurrenceController(
            EventOccurrenceService occurrenceService,
            EventService eventService) {
        this.occurrenceService = occurrenceService;
        this.eventService = eventService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Listar todas las ocurrencias",
            description = "Devuelve las ocurrencias de eventos para uso administrativo con paginacion.",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ocurrencias encontradas"),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "No autorizado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public Page<EventOccurrenceAdminResponse> getAllOccurrences(
            @ParameterObject
            @PageableDefault(size = 20, sort = "startsAt", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return occurrenceService.getAllOccurrences(pageable);
    }

    @GetMapping("/public")
    @Operation(
            summary = "Calendario publico de ocurrencias de un especialista",
            description = """
                    Devuelve las ocurrencias programadas de eventos publicados de un especialista
                    dentro de un rango de fechas, para pintar el calendario de su perfil publico.
                    """,
            security = {}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ocurrencias encontradas")
    })
    public List<EventOccurrenceCalendarResponse> getPublicCalendar(
            @Parameter(description = "ID del especialista", example = "1", required = true)
            @RequestParam Long specialistId,

            @Parameter(description = "Fecha de inicio del rango (inclusive)", required = true)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @RequestParam LocalDate dateFrom,

            @Parameter(description = "Fecha de fin del rango (inclusive)", required = true)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @RequestParam LocalDate dateTo
    ) {
        Instant from = dateFrom.atStartOfDay(TimeConfig.BUSINESS_ZONE).toInstant();
        Instant to = dateTo.plusDays(1).atStartOfDay(TimeConfig.BUSINESS_ZONE).toInstant();
        return occurrenceService.getPublicCalendar(specialistId, from, to);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSIONAL')")
    @Operation(summary = "Actualizar una ocurrencia",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public EventOccurrenceResponse updateOccurrence(
            @PathVariable Long id,
            @Valid @RequestBody EventOccurrenceRequest request,
            @AuthenticationPrincipal UserDetailsImpl principal) {
        return eventService.updateOccurrence(id, request, principal.getId(), isAdmin(principal));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSIONAL')")
    @Operation(summary = "Cambiar estado de una ocurrencia",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public EventOccurrenceResponse updateOccurrenceStatus(
            @PathVariable Long id,
            @Valid @RequestBody EventOccurrenceStatusUpdateRequest request,
            @AuthenticationPrincipal UserDetailsImpl principal) {
        return eventService.updateOccurrenceStatus(id, request, principal.getId(), isAdmin(principal));
    }

    private boolean isAdmin(UserDetailsImpl principal) {
        return principal.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
