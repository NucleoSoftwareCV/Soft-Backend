package com.hean.consigueventas.oonabe.experienceType.controller;

import com.hean.consigueventas.oonabe.common.config.OpenApiConfig;
import com.hean.consigueventas.oonabe.experienceType.dto.request.ExperienceTypeUpsertRequest;
import com.hean.consigueventas.oonabe.experienceType.dto.response.ExperienceTypeResponse;
import com.hean.consigueventas.oonabe.experienceType.service.ExperienceTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/experience-types")
@Tag(name = "Tipos de experiencia", description = "Catalogo dinamico de tipos de experiencia para eventos.")
public class ExperienceTypeController {
    private final ExperienceTypeService service;

    public ExperienceTypeController(ExperienceTypeService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar tipos de experiencia activos", security = {})
    public List<ExperienceTypeResponse> findActive() { return service.findActive(); }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos los tipos de experiencia", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public List<ExperienceTypeResponse> findAll() { return service.findAll(); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear tipo de experiencia", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public ExperienceTypeResponse create(@Valid @RequestBody ExperienceTypeUpsertRequest request) { return service.create(request); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar tipo de experiencia", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public ExperienceTypeResponse update(@PathVariable Long id, @Valid @RequestBody ExperienceTypeUpsertRequest request) { return service.update(id, request); }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activar o desactivar tipo de experiencia", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public ExperienceTypeResponse toggleStatus(@PathVariable Long id) { return service.toggleStatus(id); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un tipo sin relaciones", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public void delete(@PathVariable Long id) { service.delete(id); }
}
