package com.hean.consigueventas.oonabe.event.controller;

import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.common.enums.EventType;
import com.hean.consigueventas.oonabe.event.dto.request.CreateEventUpsertRequest;
import com.hean.consigueventas.oonabe.event.dto.request.EventFilterRequest;
import com.hean.consigueventas.oonabe.event.dto.response.CreateEventResponse;
import com.hean.consigueventas.oonabe.event.dto.response.EventCardResponse;
import com.hean.consigueventas.oonabe.event.dto.response.EventDetailResponse;
import com.hean.consigueventas.oonabe.event.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Eventos", description = "API para la gestión de eventos")
public class EventContoller {

    private final EventService eventService;

    public EventContoller(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo evento", description = "Crea un evento con su ocurrencia asociada")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Evento creado exitosamente",
                    content = @Content(schema = @Schema(implementation = CreateEventResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<CreateEventResponse> createEvent(
            @Valid @RequestBody CreateEventUpsertRequest request) {

        CreateEventResponse response = eventService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
            summary = "Listar eventos (paginado + filtros)",
            description = "Devuelve el listado paginado de eventos activos. " +
                    "Todos los parámetros de filtro son opcionales y se combinan con AND.",
            security = @SecurityRequirement(name = "")
    )
    @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente")
    public ResponseEntity<Page<EventCardResponse>> getAllEvents(
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,

            @Parameter(description = "Búsqueda por texto en título o resumen")
            @RequestParam(required = false) String search,

            @Parameter(description = "ID de categoría")
            @RequestParam(required = false) Long categoryId,

            @Parameter(description = "Tipo de experiencia: TALLER, RETIRO, CLASE, CEREMONIA, ENCUENTRO_GRUPAL, FORMACION")
            @RequestParam(required = false) EventType eventType,

            @Parameter(description = "Modalidad: ONLINE o PRESENCIAL")
            @RequestParam(required = false) EventModality modality,

            @Parameter(description = "Nombre de la ciudad (para eventos presenciales)")
            @RequestParam(required = false) String cityName,

            @Parameter(description = "Precio mínimo (usa 0 para eventos gratuitos)")
            @RequestParam(required = false) BigDecimal minPrice,

            @Parameter(description = "Precio máximo")
            @RequestParam(required = false) BigDecimal maxPrice,

            @Parameter(description = "Fecha desde (formato: yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,

            @Parameter(description = "Fecha hasta (formato: yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,

            @Parameter(description = "Hora de inicio del día (0-23). Ej: 6 para franja de mañana")
            @RequestParam(required = false) Integer hourFrom,

            @Parameter(description = "Hora de fin del día (0-23). Ej: 12 para franja de mañana")
            @RequestParam(required = false) Integer hourTo,

            @Parameter(description = "true = solo recurrentes, false = solo únicos")
            @RequestParam(required = false) Boolean isRecurring
    ) {
        EventFilterRequest filter = new EventFilterRequest(
                search, categoryId, eventType, modality, cityName,
                minPrice, maxPrice, dateFrom, dateTo, hourFrom, hourTo, isRecurring
        );
        return ResponseEntity.ok(eventService.getAllActiveEvents(filter, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle del evento",
            description = "Devuelve el detalle completo de un evento por su ID",
            security = @SecurityRequirement(name = ""))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detalle obtenido exitosamente"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado")
    })
    public ResponseEntity<EventDetailResponse> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventDetail(id));
    }

}
