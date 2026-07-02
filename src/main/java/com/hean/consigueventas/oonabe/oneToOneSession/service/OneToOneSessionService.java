package com.hean.consigueventas.oonabe.oneToOneSession.service;

import com.hean.consigueventas.oonabe.common.enums.PublicationStatus;
import com.hean.consigueventas.oonabe.common.enums.SessionModality;
import com.hean.consigueventas.oonabe.common.exception.BusinessLogicException;
import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import com.hean.consigueventas.oonabe.masterdata.entity.Location;
import com.hean.consigueventas.oonabe.masterdata.entity.Technique;
import com.hean.consigueventas.oonabe.masterdata.entity.WorkTopic;
import com.hean.consigueventas.oonabe.masterdata.repository.LocationRepository;
import com.hean.consigueventas.oonabe.masterdata.repository.TechniqueRepository;
import com.hean.consigueventas.oonabe.masterdata.repository.WorkTopicRepository;
import com.hean.consigueventas.oonabe.oneToOneSession.dto.request.OneToOneServiceRequest;
import com.hean.consigueventas.oonabe.oneToOneSession.dto.response.OneToOneServiceCardResponse;
import com.hean.consigueventas.oonabe.oneToOneSession.dto.response.OneToOneServiceResponse;
import com.hean.consigueventas.oonabe.oneToOneSession.entity.OneToOneService;
import com.hean.consigueventas.oonabe.oneToOneSession.mapper.OneToOneServiceMapper;
import com.hean.consigueventas.oonabe.oneToOneSession.repository.OneToOneServiceRepository;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import com.hean.consigueventas.oonabe.profileProfesional.repository.SpecialistProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
public class OneToOneSessionService {

    private final OneToOneServiceRepository serviceRepository;
    private final SpecialistProfileRepository specialistProfileRepository;
    private final LocationRepository locationRepository;
    private final OneToOneServiceMapper serviceMapper;
    private final WorkTopicRepository workTopicRepository;
    private final TechniqueRepository techniqueRepository;

    public OneToOneSessionService(
            OneToOneServiceRepository serviceRepository,
            SpecialistProfileRepository specialistProfileRepository,
            LocationRepository locationRepository,
            OneToOneServiceMapper serviceMapper,
            WorkTopicRepository workTopicRepository,
            TechniqueRepository techniqueRepository
    ) {
        this.serviceRepository = serviceRepository;
        this.specialistProfileRepository = specialistProfileRepository;
        this.locationRepository = locationRepository;
        this.serviceMapper = serviceMapper;
        this.workTopicRepository = workTopicRepository;
        this.techniqueRepository = techniqueRepository;
    }

    @Transactional(readOnly = true)
    public Page<OneToOneServiceCardResponse> getPublicServices(
            Long workTopicId,
            Long techniqueId,
            Pageable pageable
    ) {
        Page<OneToOneService> services;

        if (workTopicId != null && techniqueId != null) {

            services =
                    serviceRepository
                            .findDistinctByStatusAndWorkTopics_IdAndTechniques_Id(
                                    PublicationStatus.PUBLICADO,
                                    workTopicId,
                                    techniqueId,
                                    pageable
                            );

        } else if (workTopicId != null) {

            services =
                    serviceRepository
                            .findDistinctByStatusAndWorkTopics_Id(
                                    PublicationStatus.PUBLICADO,
                                    workTopicId,
                                    pageable
                            );

        } else if (techniqueId != null) {

            services =
                    serviceRepository
                            .findDistinctByStatusAndTechniques_Id(
                                    PublicationStatus.PUBLICADO,
                                    techniqueId,
                                    pageable
                            );

        } else {

            services = serviceRepository.findByStatus(
                    PublicationStatus.PUBLICADO,
                    pageable
            );
        }

        return services.map(serviceMapper::toCardDto);
    }

    @Transactional(readOnly = true)
    public List<OneToOneServiceResponse> getMyServices(Long userId) {

        SpecialistProfile specialist = getSpecialistByUserId(userId);

        return serviceRepository
                .findBySpecialistId(specialist.getId())
                .stream()
                .map(serviceMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public OneToOneServiceResponse getById(
            Long id,
            Long userId
    ) {
        OneToOneService entity = serviceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Servicio no encontrado con ID: " + id
                        )
                );

        checkAccess(entity, userId);

        return serviceMapper.toDto(entity);
    }

    @Transactional(readOnly = true)
    public OneToOneServiceResponse getBySlug(
            String slug,
            Long userId
    ) {
        OneToOneService entity = serviceRepository.findBySlug(slug)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Servicio no encontrado con slug: " + slug
                        )
                );

        checkAccess(entity, userId);

        return serviceMapper.toDto(entity);
    }

    @Transactional
    public OneToOneServiceResponse createService(
            Long userId,
            OneToOneServiceRequest request
    ) {
        SpecialistProfile specialist = getSpecialistByUserId(userId);

        OneToOneService entity = serviceMapper.toEntity(request);
        entity.setSpecialist(specialist);

        // Asociar temas de trabajo
        if (request.workTopics() != null
                && !request.workTopics().isEmpty()) {

            List<WorkTopic> workTopics =
                    workTopicRepository.findAllById(
                            request.workTopics()
                    );

            if (workTopics.size() != request.workTopics().size()) {
                throw new ResourceNotFoundException(
                        "Uno o más temas de trabajo no existen."
                );
            }

            entity.setWorkTopics(new HashSet<>(workTopics));
        }

        // Asociar técnicas
        if (request.techniques() != null
                && !request.techniques().isEmpty()) {

            List<Technique> techniques =
                    techniqueRepository.findAllById(
                            request.techniques()
                    );

            if (techniques.size() != request.techniques().size()) {
                throw new ResourceNotFoundException(
                        "Una o más técnicas no existen."
                );
            }

            entity.setTechniques(new HashSet<>(techniques));
        }

        // Asociar ubicación
        if (request.locationId() != null) {

            Location location = locationRepository
                    .findById(request.locationId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Ubicación no encontrada con ID: "
                                            + request.locationId()
                            )
                    );

            entity.setLocation(location);

        } else if (request.modality() == SessionModality.PRESENCIAL) {

            throw new BusinessLogicException(
                    "La ubicación es obligatoria para la modalidad presencial."
            );
        }

        // Asignar estado
        if (request.status() != null) {
            entity.setStatus(request.status());
        } else {
            entity.setStatus(PublicationStatus.BORRADOR);
        }

        // Generar slug
        String tempSlug = generateTempSlug(request.title());

        if (serviceRepository.existsBySlug(tempSlug)) {
            tempSlug = tempSlug + "-"
                    + (System.currentTimeMillis() % 1000);
        }

        entity.setSlug(tempSlug);

        OneToOneService saved =
                serviceRepository.save(entity);

        return serviceMapper.toDto(saved);
    }

    @Transactional
    public OneToOneServiceResponse updateService(
            Long id,
            Long userId,
            OneToOneServiceRequest request
    ) {
        // Primero se busca la entidad
        OneToOneService entity = serviceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Servicio no encontrado con ID: " + id
                        )
                );

        // Se valida que pertenezca al especialista
        SpecialistProfile specialist =
                getSpecialistByUserId(userId);

        if (!entity.getSpecialist()
                .getId()
                .equals(specialist.getId())) {

            throw new BusinessLogicException(
                    "No estás autorizado para modificar este servicio."
            );
        }

        // Actualizar datos generales
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setImageUrl(request.imageUrl());
        entity.setDurationMinutes(request.durationMinutes());
        entity.setModality(request.modality());
        entity.setPrice(request.price());

        if (request.currency() != null) {
            entity.setCurrency(request.currency());
        }

        // Actualizar temas de trabajo
        if (request.workTopics() != null) {

            List<WorkTopic> workTopics =
                    workTopicRepository.findAllById(
                            request.workTopics()
                    );

            if (workTopics.size() != request.workTopics().size()) {
                throw new ResourceNotFoundException(
                        "Uno o más temas de trabajo no existen."
                );
            }

            entity.setWorkTopics(new HashSet<>(workTopics));
        }

        // Actualizar técnicas
        if (request.techniques() != null) {

            List<Technique> techniques =
                    techniqueRepository.findAllById(
                            request.techniques()
                    );

            if (techniques.size() != request.techniques().size()) {
                throw new ResourceNotFoundException(
                        "Una o más técnicas no existen."
                );
            }

            entity.setTechniques(new HashSet<>(techniques));
        }

        // Actualizar ubicación
        if (request.locationId() != null) {

            Location location = locationRepository
                    .findById(request.locationId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Ubicación no encontrada con ID: "
                                            + request.locationId()
                            )
                    );

            entity.setLocation(location);

        } else {

            if (request.modality() == SessionModality.PRESENCIAL) {
                throw new BusinessLogicException(
                        "La ubicación es obligatoria para la modalidad presencial."
                );
            }

            entity.setLocation(null);
        }

        // Regenerar slug
        String newSlug = generateTempSlug(request.title());

        if (serviceRepository.existsBySlugAndIdNot(newSlug, id)) {
            newSlug = newSlug + "-" + id;
        }

        entity.setSlug(newSlug);

        // Guardar una sola vez
        OneToOneService updated =
                serviceRepository.save(entity);

        return serviceMapper.toDto(updated);
    }

    @Transactional
    public OneToOneServiceResponse toggleStatus(
            Long id,
            Long userId,
            PublicationStatus status
    ) {
        OneToOneService entity = serviceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Servicio no encontrado con ID: " + id
                        )
                );

        SpecialistProfile specialist =
                getSpecialistByUserId(userId);

        if (!entity.getSpecialist()
                .getId()
                .equals(specialist.getId())) {

            throw new BusinessLogicException(
                    "No estás autorizado para cambiar el estado de este servicio."
            );
        }

        entity.setStatus(status);

        OneToOneService updated =
                serviceRepository.save(entity);

        return serviceMapper.toDto(updated);
    }

    private SpecialistProfile getSpecialistByUserId(Long userId) {
        return specialistProfileRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Perfil de especialista no encontrado para el usuario actual."
                        )
                );
    }

    private void checkAccess(
            OneToOneService service,
            Long userId
    ) {
        if (service.getStatus() == PublicationStatus.PUBLICADO) {
            return;
        }

        if (userId == null) {
            throw new ResourceNotFoundException(
                    "Servicio no disponible."
            );
        }

        specialistProfileRepository
                .findByUserId(userId)
                .ifPresentOrElse(
                        specialist -> {
                            if (!service.getSpecialist()
                                    .getId()
                                    .equals(specialist.getId())) {

                                throw new ResourceNotFoundException(
                                        "Servicio no disponible."
                                );
                            }
                        },
                        () -> {
                            throw new ResourceNotFoundException(
                                    "Servicio no disponible."
                            );
                        }
                );
    }

    private String generateTempSlug(String title) {
        return title == null
                ? ""
                : title.toLowerCase()
                .replace("ñ", "n")
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}