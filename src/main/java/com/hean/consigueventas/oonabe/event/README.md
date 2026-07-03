# Modulo Event

Este modulo contiene el flujo de eventos y sus ocurrencias.

## Flujo activo

- `GET /api/v1/events`: listado publico paginado para Explorar, con filtros opcionales.
- `GET /api/v1/events/{id}`: detalle publico de un evento.
- `POST /api/v1/events`: creacion privada de un evento con una ocurrencia asociada.
- `GET /api/v1/event-occurrences`: listado administrativo de ocurrencias.

## Historial de endpoints comentados en EventOccurrenceController

Estos endpoints estaban comentados en `EventOccurrenceController` y se retiraron del codigo activo para mantener el controller limpio. Se conservan aqui como referencia para retomar el trabajo si mas adelante se necesita exponer ocurrencias directamente.

### Listar ocurrencias publicas

```java
@GetMapping("/public")
@Operation(
        summary = "Listar ocurrencias publicas",
        description = "Devuelve las ocurrencias visibles para el publico.",
        security = {}
)
@ApiResponse(responseCode = "200", description = "Ocurrencias publicas encontradas")
public List<EventOccurrencePublicResponse> getPublicOccurrences() {
    return occurrenceService.getPublicOccurrences();
}
```

### Buscar ocurrencias publicas por fecha

```java
@GetMapping("/public/date")
@Operation(summary = "Buscar ocurrencias por fecha", security = {})
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ocurrencias encontradas"),
        @ApiResponse(responseCode = "400", description = "Fecha invalida", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
})
public List<EventOccurrencePublicResponse> getPublicOccurrencesByDate(
        @Parameter(description = "Fecha de busqueda", example = "2026-06-20")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
) {
    return occurrenceService.getPublicOccurrencesByDate(date);
}
```

### Buscar ocurrencias publicas por rango de fechas

```java
@GetMapping("/public/range")
@Operation(summary = "Buscar ocurrencias por rango de fechas", security = {})
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ocurrencias encontradas"),
        @ApiResponse(responseCode = "400", description = "Rango de fechas invalido", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
})
public List<EventOccurrencePublicResponse> getPublicOccurrencesByDateRange(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
) {
    return occurrenceService.getPublicOccurrencesByDateRange(startDate, endDate);
}
```

### Listar ocurrencias de un evento

```java
@GetMapping("/event/{eventId}")
@Operation(summary = "Listar ocurrencias de un evento", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
@ApiResponse(responseCode = "200", description = "Ocurrencias encontradas")
public List<EventOccurrenceAdminResponse> getOccurrencesByEvent(@PathVariable Long eventId) {
    return occurrenceService.getOccurrencesByEvent(eventId);
}
```

### Obtener una ocurrencia

```java
@GetMapping("/{id}")
@Operation(summary = "Obtener una ocurrencia", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ocurrencia encontrada"),
        @ApiResponse(responseCode = "404", description = "Ocurrencia no encontrada", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
})
public EventOccurrenceAdminResponse getOccurrenceById(@PathVariable Long id) {
    return occurrenceService.getOccurrenceById(id);
}
```

### Filtrar ocurrencias

```java
@GetMapping("/filter")
@Operation(
        summary = "Filtrar ocurrencias",
        description = "Filtra por periodo, horario y fecha seleccionada.",
        security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
)
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ocurrencias encontradas"),
        @ApiResponse(responseCode = "400", description = "Filtro invalido", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
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
```

### Crear ocurrencia

```java
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
@Operation(summary = "Crear una ocurrencia", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
@ApiResponses({
        @ApiResponse(responseCode = "201", description = "Ocurrencia creada"),
        @ApiResponse(responseCode = "400", description = "Datos invalidos", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Evento o ubicacion no encontrados", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
})
public EventOccurrenceAdminResponse createOccurrence(@Valid @RequestBody EventOccurrenceUpsertRequest dto) {
    return occurrenceService.createOccurrence(dto);
}
```

### Actualizar ocurrencia

```java
@PutMapping("/{id}")
@Operation(summary = "Actualizar una ocurrencia", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ocurrencia actualizada"),
        @ApiResponse(responseCode = "400", description = "Datos invalidos", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Ocurrencia, evento o ubicacion no encontrados", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
})
public EventOccurrenceAdminResponse updateOccurrence(
        @PathVariable Long id,
        @Valid @RequestBody EventOccurrenceUpsertRequest dto
) {
    return occurrenceService.updateOccurrence(id, dto);
}
```

### Eliminar ocurrencia

```java
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
```

## Nota de decision

El flujo publico principal para Explorar debe seguir siendo `/api/v1/events`. Los endpoints publicos de ocurrencias se pueden reactivar si en el futuro se necesita consultar fechas sueltas sin pasar por el evento.
