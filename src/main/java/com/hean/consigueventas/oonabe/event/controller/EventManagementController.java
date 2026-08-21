package com.hean.consigueventas.oonabe.event.controller;

import com.hean.consigueventas.oonabe.common.config.OpenApiConfig;
import com.hean.consigueventas.oonabe.event.dto.request.CreateEventUpsertRequest;
import com.hean.consigueventas.oonabe.event.dto.request.EventOccurrenceRequest;
import com.hean.consigueventas.oonabe.event.dto.request.EventStatusUpdateRequest;
import com.hean.consigueventas.oonabe.event.dto.request.EventUpsertRequest;
import com.hean.consigueventas.oonabe.event.dto.response.EventOccurrenceAttendeeDto;
import com.hean.consigueventas.oonabe.event.dto.response.CreateEventResponse;
import com.hean.consigueventas.oonabe.event.dto.response.EventManagementResponse;
import com.hean.consigueventas.oonabe.event.dto.response.EventOccurrenceResponse;
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
import org.springframework.http.ProblemDetail;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Gestion de eventos", description = "API privada para gestionar eventos")
public class EventManagementController {

    private final EventService eventService;

    public EventManagementController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSIONAL')")
    @Operation(
            summary = "Crear un nuevo evento",
            description = "Crea un evento con su ocurrencia asociada",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Evento creado exitosamente",
                    content = @Content(schema = @Schema(implementation = CreateEventResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos invalidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "No autorizado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Categoria, especialista o ubicacion no encontrada",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<CreateEventResponse> createEvent(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del evento y su primera ocurrencia",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateEventUpsertRequest.class))
            )
            @Valid @RequestBody CreateEventUpsertRequest request,
            @AuthenticationPrincipal UserDetailsImpl principal) {

        CreateEventResponse response = eventService.create(
                request,
                principal.getId(),
                isAdmin(principal));
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.event().id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/my-events")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    @Operation(summary = "Listar mis eventos",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public Page<EventManagementResponse> getMyEvents(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @ParameterObject
            @PageableDefault(size = 12, sort = "updatedAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return eventService.getMyEvents(principal.getId(), pageable);
    }

    @GetMapping("/{id}/management")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSIONAL')")
    @Operation(summary = "Consultar un evento para gestion",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public EventManagementResponse getManagementEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl principal) {
        return eventService.getManagementEvent(id, principal.getId(), isAdmin(principal));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSIONAL')")
    @Operation(summary = "Actualizar un evento",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public EventManagementResponse updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventUpsertRequest request,
            @AuthenticationPrincipal UserDetailsImpl principal) {
        return eventService.updateEvent(id, request, principal.getId(), isAdmin(principal));
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSIONAL')")
    @Operation(summary = "Agregar una imagen a la galeria del evento (maximo 8)",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public EventManagementResponse addEventImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetailsImpl principal) {
        return eventService.addGalleryImage(id, file, principal.getId(), isAdmin(principal));
    }

    @DeleteMapping("/{id}/images/{imageId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSIONAL')")
    @Operation(summary = "Eliminar una imagen de la galeria del evento",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public EventManagementResponse deleteEventImage(
            @PathVariable Long id,
            @PathVariable Long imageId,
            @AuthenticationPrincipal UserDetailsImpl principal) {
        return eventService.deleteGalleryImage(id, imageId, principal.getId(), isAdmin(principal));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSIONAL')")
    @Operation(summary = "Cambiar estado de un evento",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public EventManagementResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody EventStatusUpdateRequest request,
            @AuthenticationPrincipal UserDetailsImpl principal) {
        return eventService.updateEventStatus(id, request, principal.getId(), isAdmin(principal));
    }

    @PostMapping("/{id}/occurrences")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSIONAL')")
    @Operation(summary = "Agregar una ocurrencia al evento",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public ResponseEntity<EventOccurrenceResponse> addOccurrence(
            @PathVariable Long id,
            @Valid @RequestBody EventOccurrenceRequest request,
            @AuthenticationPrincipal UserDetailsImpl principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.addOccurrence(id, request, principal.getId(), isAdmin(principal)));
    }

    @GetMapping("/occurrences/{occurrenceId}/attendees")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSIONAL')")
    @Operation(
            summary = "Listar participantes de una ocurrencia",
            description = "Retorna la lista de asistentes registrados para una fecha/sesión específica del evento.",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    )
    public List<EventOccurrenceAttendeeDto> getOccurrenceAttendees(
            @PathVariable Long occurrenceId,
            @AuthenticationPrincipal UserDetailsImpl principal) {
        return eventService.getOccurrenceAttendees(occurrenceId, principal.getId(), isAdmin(principal));
    }

    private boolean isAdmin(UserDetailsImpl principal) {
        return principal.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
