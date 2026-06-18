package com.hean.consigueventas.oonabe.catalog.controller;

import com.hean.consigueventas.oonabe.catalog.dto.response.CityResponse;
import com.hean.consigueventas.oonabe.catalog.service.ICityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cities")
@Tag(name = "Ciudades", description = "Ciudades disponibles para eventos y sesiónes.")
public class CityController {

    private final ICityService cityService;

    public CityController(ICityService cityService) {
        this.cityService = cityService;
    }

    @GetMapping
    @Operation(summary = "Listar ciudades activas", description = "Devuelve las ciudades visibles para el catálogo público.", security = {})
    @ApiResponse(responseCode = "200", description = "Ciudades activas")
    public List<CityResponse> findActive() {
        return cityService.findActive();
    }


}
