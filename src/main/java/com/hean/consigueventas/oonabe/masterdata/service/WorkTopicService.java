package com.hean.consigueventas.oonabe.masterdata.service;

import com.hean.consigueventas.oonabe.masterdata.dto.Admin.WorkTopicAdminDTO;
import com.hean.consigueventas.oonabe.masterdata.dto.User.WorkTopicPublicDTO;
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

    // Admin: lista todos los temas o filtra por estado
    @Transactional(readOnly = true)
    public List<WorkTopicAdminDTO> getAllTopics(Boolean active) {

        List<WorkTopic> topics;

        if (active == null) {
            topics = workTopicRepository.findAll();
        } else {
            topics = workTopicRepository.findByActive(active);
        }

        return topics.stream()
                .map(workTopicMapper::toAdminDto)
                .toList();
    }

    // Público: lista únicamente los temas activos
    @Transactional(readOnly = true)
    public List<WorkTopicPublicDTO> getActiveTopics() {
        return workTopicRepository.findByActive(true)
                .stream()
                .map(workTopicMapper::toPublicDto)
                .toList();
    }

    // Público: busca únicamente un tema activo por nombre
    @Transactional(readOnly = true)
    public WorkTopicPublicDTO getTopicByName(String name) {

        String normalizedName = name.trim();

        WorkTopic workTopic = workTopicRepository
                .findByNameIgnoreCaseAndActiveTrue(normalizedName)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Tema de trabajo activo no encontrado con nombre: "
                                        + normalizedName
                        )
                );

        return workTopicMapper.toPublicDto(workTopic);
    }

    // Admin: crea un tema
    @Transactional
    public WorkTopicAdminDTO createTopic(
            WorkTopicAdminDTO workTopicAdminDTO
    ) {
        String normalizedName = workTopicAdminDTO.name().trim();

        if (workTopicRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new IllegalArgumentException(
                    "Ya existe un tema de trabajo con el nombre: "
                            + normalizedName
            );
        }

        WorkTopic workTopic =
                workTopicMapper.toEntity(workTopicAdminDTO);

        workTopic.setName(normalizedName);
        workTopic.setActive(true);

        WorkTopic savedWorkTopic =
                workTopicRepository.save(workTopic);

        return workTopicMapper.toAdminDto(savedWorkTopic);
    }

    // Admin: actualiza un tema
    @Transactional
    public WorkTopicAdminDTO updateTopic(
            Long id,
            WorkTopicAdminDTO workTopicAdminDTO
    ) {
        WorkTopic existingWorkTopic = workTopicRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Tema de trabajo no encontrado con ID: " + id
                        )
                );

        String normalizedName = workTopicAdminDTO.name().trim();

        if (workTopicRepository.existsByNameIgnoreCaseAndIdNot(
                normalizedName,
                id
        )) {
            throw new IllegalArgumentException(
                    "Ya existe otro tema de trabajo con el nombre: "
                            + normalizedName
            );
        }

        workTopicMapper.updateEntityFromDto(
                workTopicAdminDTO,
                existingWorkTopic
        );

        existingWorkTopic.setName(normalizedName);

        WorkTopic updatedWorkTopic =
                workTopicRepository.save(existingWorkTopic);

        return workTopicMapper.toAdminDto(updatedWorkTopic);
    }

    // Admin: desactiva un tema
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