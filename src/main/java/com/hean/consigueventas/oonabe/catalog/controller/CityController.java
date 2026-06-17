package com.hean.consigueventas.oonabe.catalog.controller;

import com.hean.consigueventas.oonabe.catalog.dto.CityDTO;
import com.hean.consigueventas.oonabe.catalog.service.CityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/citys")
@Tag(name = "Ciudades", description = "Ciudades disponibles para eventos y sesiones.")
public class CityController {

    private final CityService cityService;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    @GetMapping
    @Operation(summary = "Listar ciudades activas", description = "Devuelve las ciudades visibles para el catalogo publico.", security = {})
    @ApiResponse(responseCode = "200", description = "Ciudades activas")
    public List<CityDTO> findActive() {
        return cityService.findActive();
    }


}
