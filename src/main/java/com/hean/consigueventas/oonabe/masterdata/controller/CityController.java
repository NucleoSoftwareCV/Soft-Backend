package com.hean.consigueventas.oonabe.masterdata.controller;

import com.hean.consigueventas.oonabe.masterdata.dto.Admin.CityAdminDTO;
import com.hean.consigueventas.oonabe.masterdata.dto.User.CityPublicDTO;
import com.hean.consigueventas.oonabe.masterdata.service.CityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cities")
@Tag(name = "Ciudades", description = "Catalogo publico y administrativo de ciudades.")
public class CityController {

    private final CityService cityService;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    // Endpoint publico
    @GetMapping
    @Operation(summary = "Listar ciudades activas", security = {})
    public List<CityPublicDTO> findActive() {
        return cityService.findActive();
    }

    // Endpoint administrativo
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar ciudades para administracion")
    public List<CityAdminDTO> findAllForAdmin(
            @RequestParam(required = false) Boolean active
    ) {
        return cityService.findAllForAdmin(active);
    }

    // Admin: crear ciudad
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear una nueva ciudad")
    public CityAdminDTO createCity(@Valid @RequestBody CityAdminDTO cityAdminDTO) {
        return cityService.createCity(cityAdminDTO);
    }

    // Admin: actualizar ciudad
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar una ciudad existente")
    public CityAdminDTO updateCity(
            @PathVariable Long id,
            @Valid @RequestBody CityAdminDTO cityAdminDTO
    ) {
        return cityService.updateCity(id, cityAdminDTO);
    }

    // Admin: eliminar ciudad
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar físicamente una ciudad")
    public void deleteCity(@PathVariable Long id) {
        cityService.deleteCity(id);
    }
}
