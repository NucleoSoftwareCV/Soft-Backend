package com.hean.consigueventas.oonabe.event.controller;

import com.hean.consigueventas.oonabe.common.dto.response.PagedResponse;
import com.hean.consigueventas.oonabe.event.dto.request.EventFilterRequest;
import com.hean.consigueventas.oonabe.event.dto.response.EventCardResponse;
import com.hean.consigueventas.oonabe.event.dto.response.EventDetailResponse;
import com.hean.consigueventas.oonabe.event.service.EventService;
import com.hean.consigueventas.oonabe.event.support.EventPageables;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Eventos", description = "API publica para explorar eventos")
@Validated
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    @Operation(
            summary = "Listar eventos (paginado + filtros)",
            description = """
                    Devuelve el listado paginado de eventos publicados para la pantalla Explorar.
                    Todos los parametros de filtro son opcionales y se combinan con AND.
                    Parametros de paginacion: page, size y sort. Orden por defecto: startsAt,asc.
                    """,
            security = {}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente"),
            @ApiResponse(responseCode = "400", description = "Parametros de filtro invalidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<PagedResponse<EventCardResponse>> getAllEvents(
            @ParameterObject
            @PageableDefault(size = 12, sort = "startsAt", direction = Sort.Direction.ASC) Pageable pageable,
            @Valid @ParameterObject EventFilterRequest filter
    ) {
        return ResponseEntity.ok(PagedResponse.from(eventService.getAllActiveEvents(filter, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener detalle del evento",
            description = "Devuelve el detalle completo de un evento por su ID",
            security = {}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detalle obtenido exitosamente"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<EventDetailResponse> getEventById(
            @Parameter(description = "ID del evento", example = "1", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(eventService.getEventDetail(id));
    }

    @GetMapping("/{id}/similar")
    @Operation(
            summary = "Listar eventos similares",
            description = """
                    Devuelve eventos publicados de la misma categoria que el evento indicado,
                    excluyendo el evento actual y eventos del mismo organizador.
                    Parametros de paginacion: page, size y sort. Orden por defecto: startsAt,asc.
                    """,
            security = {}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Eventos similares obtenidos exitosamente"),
            @ApiResponse(responseCode = "404", description = "Evento base no encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<PagedResponse<EventCardResponse>> getSimilarEvents(
            @Parameter(description = "ID del evento base", example = "1", required = true)
            @PathVariable Long id,
            @ParameterObject
            @PageableDefault(size = 4, sort = "startsAt", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(PagedResponse.from(
                eventService.getSimilarEvents(id, EventPageables.sanitizeRelatedListing(pageable))
        ));
    }

    @GetMapping("/{id}/organizer-events")
    @Operation(
            summary = "Listar otros eventos del organizador",
            description = """
                    Devuelve otros eventos publicados del mismo organizador,
                    excluyendo el evento actual.
                    Parametros de paginacion: page, size y sort. Orden por defecto: startsAt,asc.
                    """,
            security = {}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Eventos del organizador obtenidos exitosamente"),
            @ApiResponse(responseCode = "404", description = "Evento base no encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<PagedResponse<EventCardResponse>> getOrganizerEvents(
            @Parameter(description = "ID del evento base", example = "1", required = true)
            @PathVariable Long id,
            @ParameterObject
            @PageableDefault(size = 4, sort = "startsAt", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(PagedResponse.from(
                eventService.getOrganizerEvents(id, EventPageables.sanitizeRelatedListing(pageable))
        ));
    }
}
