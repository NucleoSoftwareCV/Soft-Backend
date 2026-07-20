package com.hean.consigueventas.oonabe.profileProfesional.service;

import com.hean.consigueventas.oonabe.common.enums.ApprovalStatus;
import com.hean.consigueventas.oonabe.common.enums.PublicationStatus;
import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import com.hean.consigueventas.oonabe.masterdata.entity.Technique;
import com.hean.consigueventas.oonabe.masterdata.entity.WorkTopic;
import com.hean.consigueventas.oonabe.masterdata.repository.TechniqueRepository;
import com.hean.consigueventas.oonabe.masterdata.repository.WorkTopicRepository;
import com.hean.consigueventas.oonabe.profileProfesional.dto.request.ProfessionalSocialLinkRequest;
import com.hean.consigueventas.oonabe.profileProfesional.dto.request.SpecialistProfileRequest;
import com.hean.consigueventas.oonabe.profileProfesional.dto.response.ProfessionalSocialLinkResponse;
import com.hean.consigueventas.oonabe.profileProfesional.dto.response.SpecialistProfileResponse;
import com.hean.consigueventas.oonabe.profileProfesional.entity.ProfessionalSocialLink;
import com.hean.consigueventas.oonabe.profileProfesional.entity.ProfessionalTechnique;
import com.hean.consigueventas.oonabe.profileProfesional.entity.ProfessionalWorkTopic;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import com.hean.consigueventas.oonabe.profileProfesional.mapper.ProfessionalSocialLinkMapper;
import com.hean.consigueventas.oonabe.profileProfesional.mapper.SpecialistProfileMapper;
import com.hean.consigueventas.oonabe.profileProfesional.repository.ProfessionalSocialLinkRepository;
import com.hean.consigueventas.oonabe.profileProfesional.repository.ProfessionalTechniqueRepository;
import com.hean.consigueventas.oonabe.profileProfesional.repository.ProfessionalWorkTopicRepository;
import com.hean.consigueventas.oonabe.profileProfesional.repository.SpecialistProfileRepository;
import com.hean.consigueventas.oonabe.user.entity.User;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.time.Instant;
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
    private final ProfessionalSocialLinkRepository socialLinkRepository;
    private final ProfessionalWorkTopicRepository professionalWorkTopicRepository;
    private final ProfessionalTechniqueRepository professionalTechniqueRepository;
    private final WorkTopicRepository workTopicRepository;
    private final TechniqueRepository techniqueRepository;
    private final UserRepository userRepository;
    private final SpecialistProfileMapper specialistProfileMapper;
    private final ProfessionalSocialLinkMapper socialLinkMapper;
    private final LocalImageStorageService localImageStorageService;

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

    @Transactional
    public SpecialistProfileResponse updateMyProfile(
            String username,
            SpecialistProfileRequest request
    ) {
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

        boolean publicNameChanged =
                !profile.getPublicName()
                        .equalsIgnoreCase(request.publicName().trim());

        specialistProfileMapper.updateEntityFromRequest(
                request,
                profile
        );

        profile.setProfileCategory(
                validateProfileCategory(request.profileCategory())
        );

        if (publicNameChanged) {
            profile.setSlug(
                    generateUniqueSlugForUpdate(
                            request.publicName(),
                            profile.getId()
                    )
            );
        }

        markProfileAsPendingReview(profile);

        SpecialistProfile updatedProfile =
                specialistProfileRepository.save(profile);

        if (request.workTopicIds() != null) {
            professionalWorkTopicRepository
                    .deleteBySpecialistProfileId(profile.getId());

            saveWorkTopics(
                    updatedProfile,
                    request.workTopicIds()
            );
        }

        if (request.techniqueIds() != null) {
            professionalTechniqueRepository
                    .deleteBySpecialistProfileId(profile.getId());

            saveTechniques(
                    updatedProfile,
                    request.techniqueIds()
            );
        }

        return buildResponse(updatedProfile);
    }

    @Transactional
    public SpecialistProfileResponse approveProfile(
            Long profileId,
            String adminUsername
    ) {
        SpecialistProfile profile =
                specialistProfileRepository.findById(profileId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Perfil profesional no encontrado."
                                )
                        );

        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuario administrador no encontrado."
                        )
                );

        profile.setApprovalStatus(ApprovalStatus.APROBADO);
        profile.setApprovedBy(admin);
        profile.setApprovedAt(Instant.now());
        profile.setRejectionReason(null);

        SpecialistProfile approvedProfile =
                specialistProfileRepository.save(profile);

        return buildResponse(approvedProfile);
    }

    @Transactional
    public SpecialistProfileResponse rejectProfile(
            Long profileId,
            String rejectionReason,
            String adminUsername
    ) {
        if (rejectionReason == null || rejectionReason.isBlank()) {
            throw new IllegalArgumentException(
                    "El motivo del rechazo es obligatorio."
            );
        }

        SpecialistProfile profile =
                specialistProfileRepository.findById(profileId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Perfil profesional no encontrado."
                                )
                        );

        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuario administrador no encontrado."
                        )
                );

        profile.setApprovalStatus(ApprovalStatus.RECHAZADO);
        profile.setPublicationStatus(PublicationStatus.BORRADOR);
        profile.setApprovedBy(admin);
        profile.setApprovedAt(null);
        profile.setRejectionReason(rejectionReason.trim());

        SpecialistProfile rejectedProfile =
                specialistProfileRepository.save(profile);

        return buildResponse(rejectedProfile);
    }

    @Transactional
    public SpecialistProfileResponse publishMyProfile(String username) {
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

        if (profile.getApprovalStatus() != ApprovalStatus.APROBADO) {
            throw new IllegalStateException(
                    "El perfil debe estar aprobado antes de publicarse."
            );
        }

        validateProfileReadyToPublish(profile);

        profile.setPublicationStatus(PublicationStatus.PUBLICADO);

        SpecialistProfile publishedProfile =
                specialistProfileRepository.save(profile);

        return buildResponse(publishedProfile);
    }

    @Transactional
    public SpecialistProfileResponse unpublishMyProfile(String username) {
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

        profile.setPublicationStatus(PublicationStatus.BORRADOR);

        SpecialistProfile unpublishedProfile =
                specialistProfileRepository.save(profile);

        return buildResponse(unpublishedProfile);
    }

    @Transactional(readOnly = true)
    public Page<SpecialistProfileResponse> getPublicProfiles(
            String profileCategory,
            Pageable pageable
    ) {
        Page<SpecialistProfile> profiles;

        if (profileCategory == null || profileCategory.isBlank()) {
            profiles =
                    specialistProfileRepository
                            .findByApprovalStatusAndPublicationStatus(
                                    ApprovalStatus.APROBADO,
                                    PublicationStatus.PUBLICADO,
                                    pageable
                            );
        } else {
            String normalizedCategory =
                    validateProfileCategory(profileCategory);

            profiles =
                    specialistProfileRepository
                            .findByProfileCategoryIgnoreCaseAndApprovalStatusAndPublicationStatus(
                                    normalizedCategory,
                                    ApprovalStatus.APROBADO,
                                    PublicationStatus.PUBLICADO,
                                    pageable
                            );
        }

        return profiles.map(this::buildResponse);
    }

    @Transactional(readOnly = true)
    public SpecialistProfileResponse getPublicProfileBySlug(String slug) {
        SpecialistProfile profile =
                specialistProfileRepository
                        .findBySlugAndApprovalStatusAndPublicationStatus(
                                slug,
                                ApprovalStatus.APROBADO,
                                PublicationStatus.PUBLICADO
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Perfil público no encontrado."
                                )
                        );

        return buildResponse(profile);
    }

    @Transactional(readOnly = true)
    public Page<SpecialistProfileResponse> getProfilesForAdmin(
            ApprovalStatus approvalStatus,
            PublicationStatus publicationStatus,
            Pageable pageable
    ) {
        Page<SpecialistProfile> profiles;

        if (approvalStatus == null && publicationStatus == null) {
            profiles = specialistProfileRepository.findAll(pageable);
        } else if (approvalStatus != null && publicationStatus != null) {
            profiles =
                    specialistProfileRepository
                            .findByApprovalStatusAndPublicationStatus(
                                    approvalStatus,
                                    publicationStatus,
                                    pageable
                            );
        } else if (approvalStatus != null) {
            profiles =
                    specialistProfileRepository.findByApprovalStatus(
                            approvalStatus,
                            pageable
                    );
        } else {
            profiles =
                    specialistProfileRepository.findByPublicationStatus(
                            publicationStatus,
                            pageable
                    );
        }

        return profiles.map(this::buildResponse);
    }

    @Transactional
    public ProfessionalSocialLinkResponse saveSocialLink(
            String username,
            ProfessionalSocialLinkRequest request
    ) {
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

        String platform = request.platform()
                .trim()
                .toUpperCase(Locale.ROOT);

        if (!SOCIAL_NETWORK_ORDER.containsKey(platform)) {
            throw new IllegalArgumentException(
                    "La plataforma debe ser INSTAGRAM, YOUTUBE, FACEBOOK o TIKTOK."
            );
        }

        ProfessionalSocialLink socialLink =
                socialLinkRepository
                        .findBySpecialistProfileIdAndPlatformIgnoreCase(
                                profile.getId(),
                                platform
                        )
                        .orElseGet(ProfessionalSocialLink::new);

        socialLink.setSpecialistProfile(profile);
        socialLink.setPlatform(platform);
        socialLink.setProfileUrl(request.profileUrl().trim());

        ProfessionalSocialLink savedSocialLink =
                socialLinkRepository.save(socialLink);

        markProfileAsPendingReview(profile);
        specialistProfileRepository.save(profile);

        return socialLinkMapper.toResponse(savedSocialLink);
    }

    @Transactional
    public void deleteSocialLink(
            String username,
            String platform
    ) {
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

        String normalizedPlatform = platform
                .trim()
                .toUpperCase(Locale.ROOT);

        ProfessionalSocialLink socialLink =
                socialLinkRepository
                        .findBySpecialistProfileIdAndPlatformIgnoreCase(
                                profile.getId(),
                                normalizedPlatform
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Red social no encontrada."
                                )
                        );

        socialLinkRepository.delete(socialLink);

        markProfileAsPendingReview(profile);
        specialistProfileRepository.save(profile);
    }

    @Transactional
    public SpecialistProfileResponse uploadProfilePhoto(
            String username,
            MultipartFile file
    ) {
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

        localImageStorageService.deleteImage(profile.getPhotoUrl());

        String photoUrl = localImageStorageService.saveProfilePhoto(
                file,
                profile.getId()
        );

        profile.setPhotoUrl(photoUrl);
        markProfileAsPendingReview(profile);

        SpecialistProfile updatedProfile =
                specialistProfileRepository.save(profile);

        return buildResponse(updatedProfile);
    }

    @Transactional
    public SpecialistProfileResponse uploadBanner(
            String username,
            MultipartFile file
    ) {
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

        localImageStorageService.deleteImage(profile.getBannerUrl());

        String bannerUrl = localImageStorageService.saveBanner(
                file,
                profile.getId()
        );

        profile.setBannerUrl(bannerUrl);
        markProfileAsPendingReview(profile);

        SpecialistProfile updatedProfile =
                specialistProfileRepository.save(profile);

        return buildResponse(updatedProfile);
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

        List<ProfessionalSocialLinkResponse> socialLinks =
                socialLinkRepository
                        .findBySpecialistProfileId(profile.getId())
                        .stream()
                        .sorted(
                                Comparator.comparingInt(
                                        link -> SOCIAL_NETWORK_ORDER
                                                .getOrDefault(
                                                        link.getPlatform()
                                                                .toUpperCase(Locale.ROOT),
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
                socialLinks
        );
    }

    private void markProfileAsPendingReview(
            SpecialistProfile profile
    ) {
        profile.setApprovalStatus(ApprovalStatus.PENDIENTE);
        profile.setPublicationStatus(PublicationStatus.BORRADOR);
        profile.setApprovedBy(null);
        profile.setApprovedAt(null);
        profile.setRejectionReason(null);
    }

    private void validateProfileReadyToPublish(
            SpecialistProfile profile
    ) {
        if (
                profile.getPhotoUrl() == null
                        || profile.getPhotoUrl().isBlank()
        ) {
            throw new IllegalStateException(
                    "Debe subir una foto de perfil antes de publicar."
            );
        }

        if (
                profile.getBannerUrl() == null
                        || profile.getBannerUrl().isBlank()
        ) {
            throw new IllegalStateException(
                    "Debe subir un banner antes de publicar."
            );
        }

        if (
                profile.getBiography() == null
                        || profile.getBiography().isBlank()
        ) {
            throw new IllegalStateException(
                    "Debe completar la biografía antes de publicar."
            );
        }

        if (
                profile.getWhatsappPhone() == null
                        || profile.getWhatsappPhone().isBlank()
        ) {
            throw new IllegalStateException(
                    "Debe registrar un número de WhatsApp antes de publicar."
            );
        }
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

    private String generateUniqueSlugForUpdate(
            String publicName,
            Long profileId
    ) {
        String baseSlug = Normalizer
                .normalize(publicName, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");

        String slug = baseSlug;
        int suffix = 2;

        while (
                specialistProfileRepository
                        .existsBySlugAndIdNot(slug, profileId)
        ) {
            slug = baseSlug + "-" + suffix;
            suffix++;
        }

        return slug;
    }
}