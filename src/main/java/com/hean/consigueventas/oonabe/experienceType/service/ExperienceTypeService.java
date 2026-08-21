package com.hean.consigueventas.oonabe.experienceType.service;

import com.hean.consigueventas.oonabe.common.exception.BusinessLogicException;
import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import com.hean.consigueventas.oonabe.event.repository.EventRepository;
import com.hean.consigueventas.oonabe.experienceType.dto.request.ExperienceTypeUpsertRequest;
import com.hean.consigueventas.oonabe.experienceType.dto.response.ExperienceTypeResponse;
import com.hean.consigueventas.oonabe.experienceType.entity.ExperienceType;
import com.hean.consigueventas.oonabe.experienceType.repository.ExperienceTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExperienceTypeService {
    private final ExperienceTypeRepository repository;
    private final EventRepository eventRepository;

    public ExperienceTypeService(ExperienceTypeRepository repository, EventRepository eventRepository) {
        this.repository = repository;
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public List<ExperienceTypeResponse> findActive() {
        return repository.findByActiveTrueOrderByNameAsc().stream()
                .map(this::toPublicResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExperienceTypeResponse> findAll() {
        return repository.findAllByOrderByNameAsc().stream().map(this::toResponse).toList();
    }

    @Transactional
    public ExperienceTypeResponse create(ExperienceTypeUpsertRequest request) {
        validateUniqueName(request.name(), null);
        ExperienceType type = new ExperienceType();
        type.setName(request.name().trim());
        type.setDescription(normalize(request.description()));
        return toResponse(repository.save(type));
    }

    @Transactional
    public ExperienceTypeResponse update(Long id, ExperienceTypeUpsertRequest request) {
        ExperienceType type = findById(id);
        validateUniqueName(request.name(), id);
        type.setName(request.name().trim());
        type.setDescription(normalize(request.description()));
        return toResponse(repository.save(type));
    }

    @Transactional
    public ExperienceTypeResponse toggleStatus(Long id) {
        ExperienceType type = findById(id);
        type.setActive(!type.isActive());
        return toResponse(repository.save(type));
    }

    @Transactional
    public void delete(Long id) {
        ExperienceType type = findById(id);
        if (eventRepository.existsByExperienceTypeId(id)) {
            throw new BusinessLogicException("El tipo de experiencia esta en uso. Desactivalo en lugar de eliminarlo.");
        }
        repository.delete(type);
    }

    private void validateUniqueName(String name, Long currentId) {
        boolean exists = currentId == null
                ? repository.existsByNameIgnoreCase(name.trim())
                : repository.existsByNameIgnoreCaseAndIdNot(name.trim(), currentId);
        if (exists) {
            throw new BusinessLogicException("El tipo de experiencia ya existe.");
        }
    }

    private ExperienceType findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de experiencia no encontrado con ID: " + id));
    }

    private ExperienceTypeResponse toResponse(ExperienceType type) {
        return new ExperienceTypeResponse(
                type.getId(), type.getName(), type.getSlug(), type.getDescription(), type.isActive(),
                !eventRepository.existsByExperienceTypeId(type.getId()));
    }

    private ExperienceTypeResponse toPublicResponse(ExperienceType type) {
        return new ExperienceTypeResponse(
                type.getId(), type.getName(), type.getSlug(), type.getDescription(), true, false);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
