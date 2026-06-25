package com.hean.consigueventas.oonabe.masterdata.controller;

import com.hean.consigueventas.oonabe.masterdata.dto.TechniqueDTO;
import com.hean.consigueventas.oonabe.masterdata.service.TechniqueService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/technique")
public class TechniqueController {

    private final TechniqueService techniqueService;

    public TechniqueController(TechniqueService techniqueService) {
        this.techniqueService = techniqueService;
    }

    // Solo el ADMIN puede ver las tecnicas activas e inactivas
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<TechniqueDTO> getAllTechniques() {
        return techniqueService.getAllTechniques();
    }

    // Todos pueden ver las tecnicas activos
    @GetMapping("/active")
    public List<TechniqueDTO> getActiveTechniques() {
        return techniqueService.getActiveTechniques();
    }

    // Solo el ADMIN puede buscar una tecnica por su nombre
    @GetMapping("/search")
    public TechniqueDTO getTechniqueByName(
            @RequestParam String name
    ) {
        return techniqueService.getTechniqueByName(name);
    }

    // Solo el ADMIN puede crear una tecnica
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public TechniqueDTO createTechnique(
            @Valid @RequestBody TechniqueDTO techniqueDTO
    ) {
        return techniqueService.createTechnique(techniqueDTO);
    }

    // Solo el ADMIN puede modificar una tecnica
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TechniqueDTO updateTechnique(
            @PathVariable Long id,
            @Valid @RequestBody TechniqueDTO techniqueDTO
    ) {
        return techniqueService.updateTechnique(id, techniqueDTO);
    }

    // Solo el ADMIN puede desactivar una tecnica
    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public void deactivateTechnique(@PathVariable Long id) {
        techniqueService.deactivateTechnique(id);
    }
}
