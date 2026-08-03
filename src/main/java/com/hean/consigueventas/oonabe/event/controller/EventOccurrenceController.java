package com.hean.consigueventas.oonabe.event.controller;

import com.hean.consigueventas.oonabe.common.config.OpenApiConfig;
import com.hean.consigueventas.oonabe.event.dto.response.EventOccurrenceAdminResponse;
import com.hean.consigueventas.oonabe.event.dto.response.EventOccurrenceResponse;
import com.hean.consigueventas.oonabe.event.dto.request.EventOccurrenceRequest;
import com.hean.consigueventas.oonabe.event.dto.request.EventOccurrenceStatusUpdateRequest;
import com.hean.consigueventas.oonabe.event.service.EventOccurrenceService;
import com.hean.consigueventas.oonabe.event.service.EventService;
import com.hean.consigueventas.oonabe.auth.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RestController;

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
