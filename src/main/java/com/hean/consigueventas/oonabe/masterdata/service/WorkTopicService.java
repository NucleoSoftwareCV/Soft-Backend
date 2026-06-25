package com.hean.consigueventas.oonabe.masterdata.service;

import com.hean.consigueventas.oonabe.masterdata.dto.WorkTopicDTO;
import com.hean.consigueventas.oonabe.masterdata.entity.WorkTopic;
import com.hean.consigueventas.oonabe.masterdata.mapper.WorkTopicMapper;
import com.hean.consigueventas.oonabe.masterdata.repository.WorkTopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WorkTopicService {

    private final WorkTopicRepository workTopicRepository;
    private final WorkTopicMapper workTopicMapper;

    public WorkTopicService(
            WorkTopicRepository workTopicRepository,
            WorkTopicMapper workTopicMapper
    ) {
        this.workTopicRepository = workTopicRepository;
        this.workTopicMapper = workTopicMapper;
    }

    //Listar todos los temas
    @Transactional(readOnly = true)
    public List<WorkTopicDTO> getAllTopics() {
        return workTopicRepository.findAll()
                .stream()
                .map(workTopicMapper::toDto)
                .toList();
    }

    //Listar los temas activos
    @Transactional(readOnly = true)
    public List<WorkTopicDTO> getActiveTopics() {
        return workTopicRepository.findByActive(true)
                .stream()
                .map(workTopicMapper::toDto)
                .toList();
    }

    //Buscar los temas por nombre
    @Transactional(readOnly = true)
    public WorkTopicDTO getTopicByName(String name) {
        WorkTopic workTopic = workTopicRepository
                .findByNameIgnoreCase(name.trim())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Tema de trabajo no encontrado con nombre: " + name
                        )
                );

        return workTopicMapper.toDto(workTopic);
    }

    //Crear los temas
    @Transactional
    public WorkTopicDTO createTopic(WorkTopicDTO workTopicDTO) {
        String name = workTopicDTO.name().trim();

        if (workTopicRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException(
                    "Ya existe un tema de trabajo con el nombre: " + name
            );
        }

        WorkTopic workTopic = workTopicMapper.toEntity(workTopicDTO);

        workTopic.setName(name);
        workTopic.setActive(true);

        WorkTopic savedWorkTopic =
                workTopicRepository.save(workTopic);

        return workTopicMapper.toDto(savedWorkTopic);
    }

    //Actualizar los temas
    @Transactional
    public WorkTopicDTO updateTopic(
            Long id,
            WorkTopicDTO workTopicDTO
    ) {
        WorkTopic existingWorkTopic = workTopicRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Tema de trabajo no encontrado con ID: " + id
                        )
                );

        workTopicMapper.updateEntityFromDto(
                workTopicDTO,
                existingWorkTopic
        );

        existingWorkTopic.setName(workTopicDTO.name().trim());

        WorkTopic updatedWorkTopic =
                workTopicRepository.save(existingWorkTopic);

        return workTopicMapper.toDto(updatedWorkTopic);
    }

    //Desactivar los temas
    @Transactional
    public void deactivateTopic(Long id) {
        WorkTopic workTopic = workTopicRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Tema de trabajo no encontrado con ID: " + id
                        )
                );

        workTopic.setActive(false);
        workTopicRepository.save(workTopic);
    }
}