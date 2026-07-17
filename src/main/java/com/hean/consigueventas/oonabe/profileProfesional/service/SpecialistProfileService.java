package com.hean.consigueventas.oonabe.profileProfesional.service;

import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import com.hean.consigueventas.oonabe.masterdata.entity.Technique;
import com.hean.consigueventas.oonabe.masterdata.entity.WorkTopic;
import com.hean.consigueventas.oonabe.masterdata.repository.TechniqueRepository;
import com.hean.consigueventas.oonabe.masterdata.repository.WorkTopicRepository;
import com.hean.consigueventas.oonabe.profileProfesional.dto.request.SpecialistProfileRequest;
import com.hean.consigueventas.oonabe.profileProfesional.dto.response.ProfessionalImageResponse;
import com.hean.consigueventas.oonabe.profileProfesional.dto.response.ProfessionalSocialLinkResponse;
import com.hean.consigueventas.oonabe.profileProfesional.dto.response.SpecialistProfileResponse;
import com.hean.consigueventas.oonabe.profileProfesional.entity.ProfessionalTechnique;
import com.hean.consigueventas.oonabe.profileProfesional.entity.ProfessionalWorkTopic;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import com.hean.consigueventas.oonabe.profileProfesional.mapper.ProfessionalImageMapper;
import com.hean.consigueventas.oonabe.profileProfesional.mapper.ProfessionalSocialLinkMapper;
import com.hean.consigueventas.oonabe.profileProfesional.mapper.SpecialistProfileMapper;
import com.hean.consigueventas.oonabe.profileProfesional.repository.ProfessionalImageRepository;
import com.hean.consigueventas.oonabe.profileProfesional.repository.ProfessionalSocialLinkRepository;
import com.hean.consigueventas.oonabe.profileProfesional.repository.ProfessionalTechniqueRepository;
import com.hean.consigueventas.oonabe.profileProfesional.repository.ProfessionalWorkTopicRepository;
import com.hean.consigueventas.oonabe.profileProfesional.repository.SpecialistProfileRepository;
import com.hean.consigueventas.oonabe.user.entity.User;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpecialistProfileService {

    private static final Set<String> PROFILE_CATEGORIES =
            Set.of("PROFESIONALES", "CENTRO", "ORGANIZADOR");

    private static final Map<String, Integer> SOCIAL_NETWORK_ORDER = Map.of(
            "INSTAGRAM", 1,
            "YOUTUBE", 2,
            "FACEBOOK", 3,
            "TIKTOK", 4
    );

    private final SpecialistProfileRepository specialistProfileRepository;
    private final ProfessionalImageRepository professionalImageRepository;
    private final ProfessionalSocialLinkRepository socialLinkRepository;
    private final ProfessionalWorkTopicRepository professionalWorkTopicRepository;
    private final ProfessionalTechniqueRepository professionalTechniqueRepository;

    private final WorkTopicRepository workTopicRepository;
    private final TechniqueRepository techniqueRepository;
    private final UserRepository userRepository;

    private final SpecialistProfileMapper specialistProfileMapper;
    private final ProfessionalImageMapper professionalImageMapper;
    private final ProfessionalSocialLinkMapper socialLinkMapper;

    @Transactional
    public SpecialistProfileResponse createProfile(
            String username,
            SpecialistProfileRequest request
    ) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuario autenticado no encontrado."
                        )
                );

        Long userId = user.getId();

        if (specialistProfileRepository.findByUserId(userId).isPresent()) {
            throw new IllegalStateException(
                    "El usuario ya tiene un perfil profesional."
            );
        }

        SpecialistProfile profile =
                specialistProfileMapper.toEntity(request);

        profile.setUser(user);
        profile.setSlug(generateUniqueSlug(request.publicName()));
        profile.setProfileCategory(
                validateProfileCategory(request.profileCategory())
        );

        SpecialistProfile savedProfile =
                specialistProfileRepository.save(profile);

        saveWorkTopics(savedProfile, request.workTopicIds());
        saveTechniques(savedProfile, request.techniqueIds());

        return buildResponse(savedProfile);
    }

    @Transactional(readOnly = true)
    public SpecialistProfileResponse getMyProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuario autenticado no encontrado."
                        )
                );

        SpecialistProfile profile =
                specialistProfileRepository.findByUserId(user.getId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Perfil profesional no encontrado."
                                )
                        );

        return buildResponse(profile);
    }

    private void saveWorkTopics(
            SpecialistProfile profile,
            Set<Long> workTopicIds
    ) {
        if (workTopicIds == null || workTopicIds.isEmpty()) {
            return;
        }

        List<WorkTopic> workTopics =
                workTopicRepository.findAllById(workTopicIds);

        if (workTopics.size() != workTopicIds.size()) {
            throw new ResourceNotFoundException(
                    "Uno o más temas de trabajo no existen."
            );
        }

        List<ProfessionalWorkTopic> relations =
                workTopics.stream()
                        .map(workTopic -> {
                            ProfessionalWorkTopic relation =
                                    new ProfessionalWorkTopic();

                            relation.setSpecialistProfile(profile);
                            relation.setWorkTopic(workTopic);

                            return relation;
                        })
                        .toList();

        professionalWorkTopicRepository.saveAll(relations);
    }

    private void saveTechniques(
            SpecialistProfile profile,
            Set<Long> techniqueIds
    ) {
        if (techniqueIds == null || techniqueIds.isEmpty()) {
            return;
        }

        List<Technique> techniques =
                techniqueRepository.findAllById(techniqueIds);

        if (techniques.size() != techniqueIds.size()) {
            throw new ResourceNotFoundException(
                    "Una o más técnicas no existen."
            );
        }

        List<ProfessionalTechnique> relations =
                techniques.stream()
                        .map(technique -> {
                            ProfessionalTechnique relation =
                                    new ProfessionalTechnique();

                            relation.setSpecialistProfile(profile);
                            relation.setTechnique(technique);

                            return relation;
                        })
                        .toList();

        professionalTechniqueRepository.saveAll(relations);
    }

    private SpecialistProfileResponse buildResponse(
            SpecialistProfile profile
    ) {
        Set<String> workTopics =
                professionalWorkTopicRepository
                        .findBySpecialistProfileId(profile.getId())
                        .stream()
                        .map(relation ->
                                relation.getWorkTopic().getName()
                        )
                        .collect(Collectors.toSet());

        Set<String> techniques =
                professionalTechniqueRepository
                        .findBySpecialistProfileId(profile.getId())
                        .stream()
                        .map(relation ->
                                relation.getTechnique().getName()
                        )
                        .collect(Collectors.toSet());

        List<ProfessionalImageResponse> images =
                professionalImageRepository
                        .findBySpecialistProfileIdOrderByDisplayOrderAsc(
                                profile.getId()
                        )
                        .stream()
                        .map(professionalImageMapper::toResponse)
                        .toList();

        List<ProfessionalSocialLinkResponse> socialLinks =
                socialLinkRepository
                        .findBySpecialistProfileId(profile.getId())
                        .stream()
                        .sorted(
                                Comparator.comparingInt(
                                        link -> SOCIAL_NETWORK_ORDER
                                                .getOrDefault(
                                                        link.getPlatform()
                                                                .toUpperCase(),
                                                        99
                                                )
                                )
                        )
                        .map(socialLinkMapper::toResponse)
                        .toList();

        return specialistProfileMapper.toResponse(
                profile,
                workTopics,
                techniques,
                images,
                socialLinks
        );
    }

    private String validateProfileCategory(String profileCategory) {
        if (profileCategory == null || profileCategory.isBlank()) {
            throw new IllegalArgumentException(
                    "La categoría del perfil es obligatoria."
            );
        }

        String normalizedCategory =
                profileCategory.trim().toUpperCase(Locale.ROOT);

        if (!PROFILE_CATEGORIES.contains(normalizedCategory)) {
            throw new IllegalArgumentException(
                    "La categoría debe ser PROFESIONALES, CENTRO u ORGANIZADOR."
            );
        }

        return normalizedCategory;
    }

    private String generateUniqueSlug(String publicName) {
        String baseSlug = Normalizer
                .normalize(publicName, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");

        String slug = baseSlug;
        int suffix = 2;

        while (specialistProfileRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + suffix;
            suffix++;
        }

        return slug;
    }
}