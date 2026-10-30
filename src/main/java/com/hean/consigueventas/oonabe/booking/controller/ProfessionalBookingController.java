package com.hean.consigueventas.oonabe.booking.controller;

import com.hean.consigueventas.oonabe.auth.security.UserDetailsImpl;
import com.hean.consigueventas.oonabe.booking.dto.ProfessionalBookingDtos.*;
import com.hean.consigueventas.oonabe.booking.service.ProfessionalBookingService;
import com.hean.consigueventas.oonabe.common.config.OpenApiConfig;
import com.hean.consigueventas.oonabe.common.dto.response.PagedResponse;
import com.hean.consigueventas.oonabe.common.enums.BookingStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings/professional/me")
@PreAuthorize("hasRole('PROFESSIONAL')")
@Validated
@RequiredArgsConstructor
@Tag(name = "Reservas profesionales", description = "Reservas exclusivamente de los eventos del profesional autenticado")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@ApiResponse(responseCode = "401", description = "No autenticado")
@ApiResponse(responseCode = "403", description = "Requiere rol profesional")
@ApiResponse(responseCode = "404", description = "Reserva inexistente o ajena")
public class ProfessionalBookingController {
    private final ProfessionalBookingService service;

    @GetMapping
    @Operation(summary = "Consultar mis reservas", description = "Filtros opcionales de estado, evento y ocurrencia. Orden: creación e ID descendentes.")
    @ApiResponse(responseCode = "200", description = "Página de reservas propias")
    @ApiResponse(responseCode = "400", description = "Filtros o paginación inválidos")
    public PagedResponse<Summary> list(@AuthenticationPrincipal UserDetailsImpl user,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) @Positive Long eventId,
            @RequestParam(required = false) @Positive Long occurrenceId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.list(user.getId(), status, eventId, occurrenceId, page, size);
    }

    @GetMapping("/{code}")
    @Operation(summary = "Consultar detalle de una reserva propia")
    @ApiResponse(responseCode = "200", description = "Reserva, asistentes, disponibilidad e historial")
    public Detail detail(@AuthenticationPrincipal UserDetailsImpl user, @PathVariable String code) {
        return service.detail(user.getId(), code);
    }

    @PatchMapping("/{code}/status")
    @Operation(summary = "Aceptar o rechazar una reserva pendiente", description = "Aceptar: CONFIRMADA. Rechazar: CANCELADA con motivo. No procesa pagos.")
    @ApiResponse(responseCode = "200", description = "Decisión guardada y disponibilidad actualizada")
    @ApiResponse(responseCode = "400", description = "Estado o motivo inválido")
    @ApiResponse(responseCode = "409", description = "La reserva ya cambió, tiene un pago aprobado o la fecha no admite confirmación")
    public Detail decide(@AuthenticationPrincipal UserDetailsImpl user, @PathVariable String code,
            @Valid @RequestBody Decision decision) {
        return service.decide(user.getId(), code, decision);
    }
}
