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
import com.hean.consigueventas.oonabe.oneToOneSession.specification.OneToOneServiceSpecification;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import com.hean.consigueventas.oonabe.profileProfesional.repository.SpecialistProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class OneToOneSessionService {

    private static final String DEFAULT_CURRENCY = "EUR";
    private static final Pattern NON_ASCII_MARKS = Pattern.compile("\\p{M}+");

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
    public Page<OneToOneServiceCardResponse> getPublicServices(Long workTopicId, Long techniqueId, Pageable pageable) {
        return serviceRepository
                .findAll(OneToOneServiceSpecification.publicListing(workTopicId, techniqueId), pageable)
                .map(serviceMapper::toCardDto);
    }

    @Transactional(readOnly = true)
    public List<OneToOneServiceResponse> getMyServices(Long userId) {
        SpecialistProfile specialist = getSpecialistByUserId(userId);
        return serviceRepository.findBySpecialistId(specialist.getId())
                .stream()
                .map(serviceMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public OneToOneServiceResponse getById(Long id, Long userId) {
        OneToOneService entity = findServiceById(id);
        checkAccess(entity, userId);
        return serviceMapper.toDto(entity);
    }

    @Transactional(readOnly = true)
    public OneToOneServiceResponse getBySlug(String slug, Long userId) {
        OneToOneService entity = serviceRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con slug: " + slug));
        checkAccess(entity, userId);
        return serviceMapper.toDto(entity);
    }

    @Transactional
    public OneToOneServiceResponse createService(Long userId, OneToOneServiceRequest request) {
        SpecialistProfile specialist = getSpecialistByUserId(userId);

        OneToOneService entity = serviceMapper.toEntity(request);
        entity.setSpecialist(specialist);
        entity.setCurrency(normalizeCurrency(request.currency()));
        entity.setStatus(request.status() == null ? PublicationStatus.BORRADOR : request.status());
        entity.setWorkTopics(resolveWorkTopics(request.workTopics()));
        entity.setTechniques(resolveTechniques(request.techniques()));
        entity.setLocation(resolveLocation(request.modality(), request.locationId()));
        entity.setSlug(generateUniqueSlug(request.title(), null));

        return serviceMapper.toDto(serviceRepository.save(entity));
    }

    @Transactional
    public OneToOneServiceResponse updateService(Long id, Long userId, OneToOneServiceRequest request) {
        OneToOneService entity = findServiceById(id);
        validateOwner(entity, userId);

        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setImageUrl(request.imageUrl());
        entity.setDurationMinutes(request.durationMinutes());
        entity.setModality(request.modality());
        entity.setPrice(request.price());
        entity.setCurrency(normalizeCurrency(request.currency()));
        entity.setLocation(resolveLocation(request.modality(), request.locationId()));
        entity.setSlug(generateUniqueSlug(request.title(), id));

        if (request.workTopics() != null) {
            entity.setWorkTopics(resolveWorkTopics(request.workTopics()));
        }
        if (request.techniques() != null) {
            entity.setTechniques(resolveTechniques(request.techniques()));
        }

        return serviceMapper.toDto(serviceRepository.save(entity));
    }

    @Transactional
    public OneToOneServiceResponse toggleStatus(Long id, Long userId, PublicationStatus status) {
        OneToOneService entity = findServiceById(id);
        validateOwner(entity, userId);
        entity.setStatus(status);
        return serviceMapper.toDto(serviceRepository.save(entity));
    }

    private OneToOneService findServiceById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con ID: " + id));
    }

    private SpecialistProfile getSpecialistByUserId(Long userId) {
        return specialistProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de especialista no encontrado para el usuario actual."));
    }

    private Set<WorkTopic> resolveWorkTopics(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }

        List<WorkTopic> workTopics = workTopicRepository.findAllById(ids);
        if (workTopics.size() != ids.size()) {
            throw new ResourceNotFoundException("Uno o mas temas de trabajo no existen.");
        }
        return new HashSet<>(workTopics);
    }

    private Set<Technique> resolveTechniques(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }

        List<Technique> techniques = techniqueRepository.findAllById(ids);
        if (techniques.size() != ids.size()) {
            throw new ResourceNotFoundException("Una o mas tecnicas no existen.");
        }
        return new HashSet<>(techniques);
    }

    private Location resolveLocation(SessionModality modality, Long locationId) {
        if (locationId != null) {
            return locationRepository.findById(locationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Ubicacion no encontrada con ID: " + locationId));
        }

        if (modality == SessionModality.PRESENCIAL) {
            throw new BusinessLogicException("La ubicacion es obligatoria para la modalidad presencial.");
        }

        return null;
    }

    private void checkAccess(OneToOneService service, Long userId) {
        if (service.getStatus() == PublicationStatus.PUBLICADO) {
            return;
        }

        if (userId == null) {
            throw new ResourceNotFoundException("Servicio no disponible.");
        }

        validateOwner(service, userId);
    }

    private void validateOwner(OneToOneService service, Long userId) {
        SpecialistProfile specialist = getSpecialistByUserId(userId);
        if (!service.getSpecialist().getId().equals(specialist.getId())) {
            throw new ResourceNotFoundException("Servicio no disponible.");
        }
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            return DEFAULT_CURRENCY;
        }
        return currency.trim().toUpperCase(Locale.ROOT);
    }

    private String generateUniqueSlug(String title, Long currentServiceId) {
        String baseSlug = slugify(title);
        String candidate = baseSlug;
        int suffix = 2;

        while (slugExists(candidate, currentServiceId)) {
            candidate = baseSlug + "-" + suffix;
            suffix++;
        }

        return candidate;
    }

    private boolean slugExists(String slug, Long currentServiceId) {
        if (currentServiceId == null) {
            return serviceRepository.existsBySlug(slug);
        }
        return serviceRepository.existsBySlugAndIdNot(slug, currentServiceId);
    }

    private String slugify(String value) {
        if (value == null || value.isBlank()) {
            return "sesion";
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        return NON_ASCII_MARKS.matcher(normalized)
                .replaceAll("")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}
