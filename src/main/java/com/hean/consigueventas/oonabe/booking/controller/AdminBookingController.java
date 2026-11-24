package com.hean.consigueventas.oonabe.booking.controller;

import com.hean.consigueventas.oonabe.booking.dto.AdminBookingDtos.Row;
import com.hean.consigueventas.oonabe.booking.service.AdminBookingService;
import com.hean.consigueventas.oonabe.common.config.OpenApiConfig;
import com.hean.consigueventas.oonabe.common.dto.response.PagedResponse;
import com.hean.consigueventas.oonabe.common.enums.BookingStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/admin/bookings")
@PreAuthorize("hasRole('ADMIN')")
@Validated
@RequiredArgsConstructor
@Tag(name = "Reservas (admin)", description = "Supervisión de reservas, pagos y cancelaciones")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class AdminBookingController {
    private final AdminBookingService service;

    @GetMapping
    @Operation(summary = "Listar reservas", description = "Filtros opcionales de estado y fecha de solicitud. Orden: más recientes primero.")
    public PagedResponse<Row> list(@RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.list(status, from, to, page, size);
    }
}
