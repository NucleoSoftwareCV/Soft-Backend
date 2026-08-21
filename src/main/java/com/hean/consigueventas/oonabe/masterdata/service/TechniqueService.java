package com.hean.consigueventas.oonabe.masterdata.service;

import com.hean.consigueventas.oonabe.masterdata.dto.Admin.TechniqueAdminDTO;
import com.hean.consigueventas.oonabe.masterdata.dto.User.TechniquePublicDTO;
import com.hean.consigueventas.oonabe.masterdata.entity.Technique;
import com.hean.consigueventas.oonabe.masterdata.mapper.TechniqueMapper;
import com.hean.consigueventas.oonabe.masterdata.repository.TechniqueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TechniqueService {

    private final TechniqueRepository techniqueRepository;
    private final TechniqueMapper techniqueMapper;

    public TechniqueService(
            TechniqueRepository techniqueRepository,
            TechniqueMapper techniqueMapper
    ) {
        this.techniqueRepository = techniqueRepository;
        this.techniqueMapper = techniqueMapper;
    }

    // Admin: lista todas o filtra por estado
    @Transactional(readOnly = true)
    public List<TechniqueAdminDTO> getAllTechniques(Boolean active) {

        List<Technique> techniques;

        if (active == null) {
            techniques = techniqueRepository.findAll();
        } else {
            techniques = techniqueRepository.findByActive(active);
        }

        return techniques.stream()
                .map(techniqueMapper::toAdminDto)
                .toList();
    }

    // Público: lista únicamente las técnicas activas
    @Transactional(readOnly = true)
    public List<TechniquePublicDTO> getActiveTechniques() {
        return techniqueRepository.findByActive(true)
                .stream()
                .map(techniqueMapper::toPublicDto)
                .toList();
    }

    // Público: busca únicamente una técnica activa por nombre
    @Transactional(readOnly = true)
    public TechniquePublicDTO getTechniqueByName(String name) {

        String normalizedName = name.trim();

        Technique technique = techniqueRepository
                .findByNameIgnoreCaseAndActiveTrue(normalizedName)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Técnica activa no encontrada con nombre: "
                                        + normalizedName
                        )
                );

        return techniqueMapper.toPublicDto(technique);
    }

    // Admin: crea una técnica
    @Transactional
    public TechniqueAdminDTO createTechnique(
            TechniqueAdminDTO techniqueAdminDTO
    ) {
        String normalizedName = techniqueAdminDTO.name().trim();

        if (techniqueRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new IllegalArgumentException(
                    "Ya existe una técnica con el nombre: "
                            + normalizedName
            );
        }

        Technique technique =
                techniqueMapper.toEntity(techniqueAdminDTO);

        technique.setName(normalizedName);
        technique.setActive(true);

        Technique savedTechnique =
                techniqueRepository.save(technique);

        return techniqueMapper.toAdminDto(savedTechnique);
    }

    // Admin: actualiza una técnica
    @Transactional
    public TechniqueAdminDTO updateTechnique(
            Long id,
            TechniqueAdminDTO techniqueAdminDTO
    ) {
        Technique existingTechnique = techniqueRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Técnica no encontrada con ID: " + id
                        )
                );

        String normalizedName = techniqueAdminDTO.name().trim();

        if (techniqueRepository.existsByNameIgnoreCaseAndIdNot(
                normalizedName,
                id
        )) {
            throw new IllegalArgumentException(
                    "Ya existe otra técnica con el nombre: "
                            + normalizedName
            );
        }

        techniqueMapper.updateEntityFromDto(
                techniqueAdminDTO,
                existingTechnique
        );

        existingTechnique.setName(normalizedName);

        Technique updatedTechnique =
                techniqueRepository.save(existingTechnique);

        return techniqueMapper.toAdminDto(updatedTechnique);
    }

    // Admin: desactiva una técnica sin eliminarla
    @Transactional
    public void deactivateTechnique(Long id) {
        Technique technique = techniqueRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Técnica no encontrada con ID: " + id
                        )
                );

        technique.setActive(false);
        techniqueRepository.save(technique);
    }
}