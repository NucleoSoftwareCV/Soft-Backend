package com.hean.consigueventas.oonabe.masterdata.service;

import com.hean.consigueventas.oonabe.masterdata.dto.TechniqueDTO;
import com.hean.consigueventas.oonabe.masterdata.dto.WorkTopicDTO;
import com.hean.consigueventas.oonabe.masterdata.entity.Technique;
import com.hean.consigueventas.oonabe.masterdata.entity.WorkTopic;
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

    //Listar todas las tecnicas
    @Transactional(readOnly = true)
    public List<TechniqueDTO> getAllTechniques() {
        return techniqueRepository.findAll()
                .stream()
                .map(techniqueMapper::toDto)
                .toList();
    }

    //Listar las tecnicas activas
    @Transactional(readOnly = true)
    public List<TechniqueDTO> getActiveTechniques() {
        return techniqueRepository.findByActive(true)
                .stream()
                .map(techniqueMapper::toDto)
                .toList();
    }

    //Buscar las tecnicas por nombre
    @Transactional(readOnly = true)
    public TechniqueDTO getTechniqueByName(String name) {
        Technique technique = techniqueRepository
                .findByNameIgnoreCase(name.trim())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Tecnica de trabajo no encontrado con nombre: " + name
                        )
                );

        return techniqueMapper.toDto(technique);
    }

    //Crear topics
    @Transactional
    public TechniqueDTO createTechnique(TechniqueDTO techniqueDTO) {
        String name = techniqueDTO.name().trim();

        if (techniqueRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException(
                    "Ya existe una tecnica de trabajo con el nombre: " + name
            );
        }

        Technique technique = techniqueMapper.toEntity(techniqueDTO);

        technique.setName(name);
        technique.setActive(true);

        Technique savedTechnique =
                techniqueRepository.save(technique);

        return techniqueMapper.toDto(savedTechnique);
    }

    //Actualizar las tecnicas
    @Transactional
    public TechniqueDTO updateTechnique(
            Long id,
            TechniqueDTO techniqueDTO
    ) {
        Technique existingTechnique = techniqueRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Tecnica de trabajo no encontrado con ID: " + id
                        )
                );

        techniqueMapper.updateEntityFromDto(
                techniqueDTO,
                existingTechnique
        );

        existingTechnique.setName(techniqueDTO.name().trim());

        Technique updatedTechnique =
                techniqueRepository.save(existingTechnique);

        return techniqueMapper.toDto(updatedTechnique);
    }

    //Desactivar topics
    @Transactional
    public void deactivateTechnique(Long id) {
        Technique technique = techniqueRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Tecnica de trabajo no encontrado con ID: " + id
                        )
                );

        technique.setActive(false);
        techniqueRepository.save(technique);
    }
}
