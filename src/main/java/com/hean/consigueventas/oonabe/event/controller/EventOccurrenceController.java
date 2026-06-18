package com.hean.consigueventas.oonabe.event.controller;

import com.hean.consigueventas.oonabe.event.dto.response.EventOccurrenceAdminResponse;
import com.hean.consigueventas.oonabe.event.dto.response.EventOccurrencePublicResponse;
import com.hean.consigueventas.oonabe.event.dto.request.EventOccurrenceUpsertRequest;
import com.hean.consigueventas.oonabe.event.service.IEventOccurrenceService;
import com.hean.consigueventas.oonabe.common.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDate;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/event-occurrences")
@Tag(name = "Ocurrencias de eventos", description = "Fechas y horarios programados para los eventos.")
public class EventOccurrenceController {

    private final IEventOccurrenceService occurrenceService;

    public EventOccurrenceController(IEventOccurrenceService occurrenceService) {
        this.occurrenceService = occurrenceService;
    }

    @GetMapping
    @Operation(summary = "Listar todas las ocurrencias", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    @ApiResponse(responseCode = "200", description = "Ocurrencias encontradas")
    public List<EventOccurrenceAdminResponse> getAllOccurrences() {
        return occurrenceService.getAllOccurrences();
    }

    @GetMapping("/public")
    @Operation(summary = "Listar ocurrencias públicas", description = "Devuelve las ocurrencias visibles para el público.", security = {})
    @ApiResponse(responseCode = "200", description = "Ocurrencias públicas encontradas")
    public List<EventOccurrencePublicResponse> getPublicOccurrences() {
        return occurrenceService.getPublicOccurrences();
    }

    @GetMapping("/public/date")
    @Operation(summary = "Buscar ocurrencias por fecha", security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ocurrencias encontradas"),
            @ApiResponse(responseCode = "400", description = "Fecha inválida", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public List<EventOccurrencePublicResponse> getPublicOccurrencesByDate(
            @Parameter(description = "Fecha de búsqueda", example = "2026-06-20")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return occurrenceService.getPublicOccurrencesByDate(date);
    }

    @GetMapping("/public/range")
    @Operation(summary = "Buscar ocurrencias por rango de fechas", security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ocurrencias encontradas"),
            @ApiResponse(responseCode = "400", description = "Rango de fechas inválido", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public List<EventOccurrencePublicResponse> getPublicOccurrencesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return occurrenceService.getPublicOccurrencesByDateRange(startDate, endDate);
    }

    @GetMapping("/event/{eventId}")
    @Operation(summary = "Listar ocurrencias de un evento", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    @ApiResponse(responseCode = "200", description = "Ocurrencias encontradas")
    public List<EventOccurrenceAdminResponse> getOccurrencesByEvent(@PathVariable Long eventId) {
        return occurrenceService.getOccurrencesByEvent(eventId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una ocurrencia", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ocurrencia encontrada"),
            @ApiResponse(responseCode = "404", description = "Ocurrencia no encontrada", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public EventOccurrenceAdminResponse getOccurrenceById(@PathVariable Long id) {
        return occurrenceService.getOccurrenceById(id);
    }

    @GetMapping("/filter")
    @Operation(summary = "Filtrar ocurrencias", description = "Filtra por periodo, horario y fecha seleccionada.", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ocurrencias encontradas"),
            @ApiResponse(responseCode = "400", description = "Filtro inválido", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public List<EventOccurrenceAdminResponse> filterOccurrences(
            @RequestParam(required = false) String dateFilter,
            @RequestParam(required = false) String timeFilter,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate selectedDate
    ) {
        return occurrenceService.filterOccurrences(dateFilter, timeFilter, selectedDate);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear una ocurrencia", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ocurrencia creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Evento o ubicación no encontrados", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public EventOccurrenceAdminResponse createOccurrence(@Valid @RequestBody EventOccurrenceUpsertRequest dto) {
        return occurrenceService.createOccurrence(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una ocurrencia", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ocurrencia actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Ocurrencia, evento o ubicación no encontrados", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public EventOccurrenceAdminResponse updateOccurrence(
            @PathVariable Long id,
            @Valid @RequestBody EventOccurrenceUpsertRequest dto
    ) {
        return occurrenceService.updateOccurrence(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar una ocurrencia", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ocurrencia eliminada"),
            @ApiResponse(responseCode = "404", description = "Ocurrencia no encontrada", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public void deleteOccurrence(@PathVariable Long id) {
        occurrenceService.deleteOccurrence(id);
    }
}
