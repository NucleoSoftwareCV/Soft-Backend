package com.hean.consigueventas.oonabe.masterdata.controller;

import com.hean.consigueventas.oonabe.masterdata.dto.Admin.TechniqueAdminDTO;
import com.hean.consigueventas.oonabe.masterdata.dto.User.TechniquePublicDTO;
import com.hean.consigueventas.oonabe.masterdata.service.TechniqueService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/techniques")
public class TechniqueController {

    private final TechniqueService techniqueService;

    public TechniqueController(TechniqueService techniqueService) {
        this.techniqueService = techniqueService;
    }

    // Admin: ve todas o filtra por estado
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<TechniqueAdminDTO> getAllTechniques(
            @RequestParam(required = false) Boolean active
    ) {
        return techniqueService.getAllTechniques(active);
    }

    // Público: solo ve técnicas activas
    @GetMapping("/active")
    public List<TechniquePublicDTO> getActiveTechniques() {
        return techniqueService.getActiveTechniques();
    }

    // Público: solo puede buscar técnicas activas
    @GetMapping("/search")
    public TechniquePublicDTO getTechniqueByName(
            @RequestParam String name
    ) {
        return techniqueService.getTechniqueByName(name);
    }

    // Admin: crea una técnica
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public TechniqueAdminDTO createTechnique(
            @Valid @RequestBody TechniqueAdminDTO techniqueAdminDTO
    ) {
        return techniqueService.createTechnique(techniqueAdminDTO);
    }

    // Admin: actualiza una técnica
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TechniqueAdminDTO updateTechnique(
            @PathVariable Long id,
            @Valid @RequestBody TechniqueAdminDTO techniqueAdminDTO
    ) {
        return techniqueService.updateTechnique(
                id,
                techniqueAdminDTO
        );
    }

    // Admin: desactiva una técnica
    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public void deactivateTechnique(@PathVariable Long id) {
        techniqueService.deactivateTechnique(id);
    }
}