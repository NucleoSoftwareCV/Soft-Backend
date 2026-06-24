package com.hean.consigueventas.oonabe.event.controller;

import com.hean.consigueventas.oonabe.event.dto.request.CreateEventUpsertRequest;
import com.hean.consigueventas.oonabe.event.dto.response.CreateEventResponse;
import com.hean.consigueventas.oonabe.event.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hean.consigueventas.oonabe.event.dto.response.EventDetailResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

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
    @Operation(summary = "Listar todos los eventos", description = "Devuelve el listado de todos los eventos con su organizador y ocurrencias")
    @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente")
    public ResponseEntity<List<EventDetailResponse>> getAllEvents() {
        List<EventDetailResponse> events = eventService.getAllActiveEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle del evento", description = "Devuelve el detalle de un evento específico por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detalle del evento obtenido exitosamente"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado")
    })
    public ResponseEntity<EventDetailResponse> getEventById(@PathVariable Long id) {
        EventDetailResponse eventDetail = eventService.getEventDetail(id);
        return ResponseEntity.ok(eventDetail);
    }

}
