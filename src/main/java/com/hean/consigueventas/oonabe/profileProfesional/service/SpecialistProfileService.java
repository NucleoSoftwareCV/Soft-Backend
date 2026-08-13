package com.hean.consigueventas.oonabe.profileProfesional.service;

import com.hean.consigueventas.oonabe.common.enums.ApprovalStatus;
import com.hean.consigueventas.oonabe.common.enums.PublicationStatus;
import com.hean.consigueventas.oonabe.common.exception.BusinessLogicException;
import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import com.hean.consigueventas.oonabe.masterdata.entity.Technique;
import com.hean.consigueventas.oonabe.masterdata.entity.WorkTopic;
import com.hean.consigueventas.oonabe.masterdata.repository.TechniqueRepository;
import com.hean.consigueventas.oonabe.masterdata.repository.WorkTopicRepository;
import com.hean.consigueventas.oonabe.profileProfesional.dto.request.ProfessionalLanguageRequest;
import com.hean.consigueventas.oonabe.profileProfesional.dto.request.ProfessionalSocialLinkRequest;
import com.hean.consigueventas.oonabe.profileProfesional.dto.request.SpecialistProfilePartialUpdateRequest;
import com.hean.consigueventas.oonabe.profileProfesional.dto.request.SpecialistProfileRequest;
import com.hean.consigueventas.oonabe.profileProfesional.dto.response.GalleryImageResponse;
import com.hean.consigueventas.oonabe.profileProfesional.dto.response.ProfessionalLanguageResponse;
import com.hean.consigueventas.oonabe.profileProfesional.dto.response.ProfessionalSocialLinkResponse;
import com.hean.consigueventas.oonabe.profileProfesional.dto.response.SpecialistProfileResponse;
import com.hean.consigueventas.oonabe.profileProfesional.entity.*;
import com.hean.consigueventas.oonabe.profileProfesional.mapper.ProfessionalGalleryImageMapper;
import com.hean.consigueventas.oonabe.profileProfesional.mapper.ProfessionalLanguageMapper;
import com.hean.consigueventas.oonabe.profileProfesional.mapper.ProfessionalSocialLinkMapper;
import com.hean.consigueventas.oonabe.profileProfesional.mapper.SpecialistProfileMapper;
import com.hean.consigueventas.oonabe.profileProfesional.repository.*;
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
import java.util.ArrayList;
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

    private static final int MAX_GALLERY_IMAGES = 12;

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
    private final ProfessionalLanguageRepository professionalLanguageRepository;
    private final ProfessionalLanguageMapper professionalLanguageMapper;
    private final ProfessionalGalleryImageRepository galleryImageRepository;
    private final ProfessionalGalleryImageMapper galleryImageMapper;

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
        profile.setApprovalStatus(ApprovalStatus.APROBADO);
        profile.setPublicationStatus(PublicationStatus.BORRADOR);
        profile.setApprovedAt(Instant.now());
        profile.setRejectionReason(null);

        SpecialistProfile savedProfile =
                specialistProfileRepository.save(profile);

        saveWorkTopics(savedProfile, request.workTopicIds());
        saveTechniques(savedProfile, request.techniqueIds());

        return buildResponse(savedProfile);
    }

    /**
     * Crea un perfil profesional mínimo para un usuario recién aprobado, si todavía no tiene uno.
     * Pensado para invocarse automáticamente al aprobar una solicitud profesional, reutilizando los
     * datos ya declarados en esa solicitud (nombre, WhatsApp) en vez de dejar al usuario sin perfil
     * hasta que complete manualmente el formulario de creación.
     */
    @Transactional
    public void createMinimalProfileIfMissing(
            User user,
            String publicName,
            String whatsappPhone
    ) {
        if (specialistProfileRepository.findByUserId(user.getId()).isPresent()) {
            return;
        }

        SpecialistProfile profile = new SpecialistProfile();
        profile.setUser(user);
        profile.setPublicName(publicName);
        profile.setSlug(generateUniqueSlug(publicName));
        profile.setProfileCategory("PROFESIONALES");
        // Keep automatic profiles compatible with databases created from the
        // previous schema, where these optional columns were NOT NULL.
        profile.setBiography("");
        profile.setPhotoUrl("");
        profile.setWhatsappPhone(
                whatsappPhone != null && !whatsappPhone.isBlank() ? whatsappPhone.trim() : ""
        );
        profile.setApprovalStatus(ApprovalStatus.APROBADO);
        profile.setPublicationStatus(PublicationStatus.BORRADOR);
        profile.setApprovedAt(Instant.now());

        specialistProfileRepository.save(profile);
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
    public SpecialistProfileResponse updateMyProfilePartial(
            String username,
            SpecialistProfilePartialUpdateRequest request
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

        if (request.publicName() != null) {
            String publicName = requireText(
                    request.publicName(),
                    "El nombre público no puede estar vacío."
            );

            if (!profile.getPublicName().equalsIgnoreCase(publicName)) {
                profile.setPublicName(publicName);
                profile.setSlug(
                        generateUniqueSlugForUpdate(
                                publicName,
                                profile.getId()
                        )
                );
            }
        }

        if (request.profileCategory() != null) {
            profile.setProfileCategory(
                    validateProfileCategory(request.profileCategory())
            );
        }

        if (request.biography() != null) {
            profile.setBiography(request.biography().trim());
        }

        if (request.description() != null) {
            profile.setDescription(request.description().trim());
        }

        if (request.whatsappPhone() != null) {
            profile.setWhatsappPhone(
                    requireText(
                            request.whatsappPhone(),
                            "El número de WhatsApp no puede estar vacío."
                    )
            );
        }

        if (request.phoneNumber() != null) {
            profile.setPhoneNumber(
                    cleanOptionalText(request.phoneNumber())
            );
        }

        if (request.publicEmail() != null) {
            profile.setPublicEmail(
                    cleanOptionalText(request.publicEmail())
            );
        }

        if (request.website() != null) {
            profile.setWebsite(
                    cleanOptionalText(request.website())
            );
        }

        if (request.showUpcomingEvents() != null) {
            profile.setShowUpcomingEvents(request.showUpcomingEvents());
        }

        if (request.showOneToOneSessions() != null) {
            profile.setShowOneToOneSessions(request.showOneToOneSessions());
        }

        if (request.showGallery() != null) {
            profile.setShowGallery(request.showGallery());
        }

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
            throw new BusinessLogicException(
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
        return getPublicProfiles(profileCategory, null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<SpecialistProfileResponse> getPublicProfiles(
            String profileCategory,
            String search,
            Pageable pageable
    ) {
        Page<SpecialistProfile> profiles;

        if (search != null && !search.isBlank()) {
            String normalizedCategory = profileCategory == null || profileCategory.isBlank()
                    ? null
                    : validateProfileCategory(profileCategory);

            profiles =
                    specialistProfileRepository
                            .searchPublicProfiles(
                                    normalizedCategory,
                                    ApprovalStatus.APROBADO,
                                    PublicationStatus.PUBLICADO,
                                    search.trim(),
                                    pageable
                            );
        } else if (profileCategory == null || profileCategory.isBlank()) {
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

        markProfileAsDraft(profile);
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

        markProfileAsDraft(profile);
        specialistProfileRepository.save(profile);
    }

    @Transactional
    public ProfessionalLanguageResponse saveLanguage(
            String username,
            ProfessionalLanguageRequest request
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

        String languageName = requireText(
                request.languageName(),
                "El idioma no puede estar vacío."
        );

        ProfessionalLanguage language =
                professionalLanguageRepository
                        .findBySpecialistProfileIdAndLanguageNameIgnoreCase(
                                profile.getId(),
                                languageName
                        )
                        .orElseGet(ProfessionalLanguage::new);

        language.setSpecialistProfile(profile);
        language.setLanguageName(languageName);

        ProfessionalLanguage savedLanguage =
                professionalLanguageRepository.save(language);

        markProfileAsDraft(profile);
        specialistProfileRepository.save(profile);

        return professionalLanguageMapper.toResponse(savedLanguage);
    }

    @Transactional
    public void deleteLanguage(
            String username,
            Long languageId
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

        ProfessionalLanguage language =
                professionalLanguageRepository
                        .findBySpecialistProfileIdAndId(
                                profile.getId(),
                                languageId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Idioma no encontrado."
                                )
                        );

        professionalLanguageRepository.delete(language);

        markProfileAsDraft(profile);
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

        String previousPhotoUrl = profile.getPhotoUrl();
        String photoUrl = localImageStorageService.saveProfilePhoto(
                file,
                profile.getId()
        );

        profile.setPhotoUrl(photoUrl);
        markProfileAsDraft(profile);

        SpecialistProfile updatedProfile =
                specialistProfileRepository.save(profile);

        localImageStorageService.deleteImage(previousPhotoUrl);

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

        String previousBannerUrl = profile.getBannerUrl();
        String bannerUrl = localImageStorageService.saveBanner(
                file,
                profile.getId()
        );

        profile.setBannerUrl(bannerUrl);
        markProfileAsDraft(profile);

        SpecialistProfile updatedProfile =
                specialistProfileRepository.save(profile);

        localImageStorageService.deleteImage(previousBannerUrl);

        return buildResponse(updatedProfile);
    }

    @Transactional
    public SpecialistProfileResponse uploadGalleryImage(
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

        long currentCount =
                galleryImageRepository.countBySpecialistProfileId(profile.getId());

        if (currentCount >= MAX_GALLERY_IMAGES) {
            throw new BusinessLogicException(
                    "Solo puedes subir hasta " + MAX_GALLERY_IMAGES + " imágenes a tu galería."
            );
        }

        String imageUrl = localImageStorageService.saveGalleryImage(
                file,
                profile.getId()
        );

        ProfessionalGalleryImage image = new ProfessionalGalleryImage();
        image.setSpecialistProfile(profile);
        image.setImageUrl(imageUrl);
        image.setSortOrder((int) currentCount);

        galleryImageRepository.save(image);

        markProfileAsDraft(profile);

        SpecialistProfile updatedProfile =
                specialistProfileRepository.save(profile);

        return buildResponse(updatedProfile);
    }

    @Transactional
    public void deleteGalleryImage(
            String username,
            Long imageId
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

        ProfessionalGalleryImage image =
                galleryImageRepository
                        .findBySpecialistProfileIdAndId(
                                profile.getId(),
                                imageId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Imagen de galería no encontrada."
                                )
                        );

        galleryImageRepository.delete(image);
        localImageStorageService.deleteImage(image.getImageUrl());

        markProfileAsDraft(profile);
        specialistProfileRepository.save(profile);
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

        List<ProfessionalLanguageResponse> languages =
                professionalLanguageRepository
                        .findBySpecialistProfileIdOrderByLanguageNameAsc(
                                profile.getId()
                        )
                        .stream()
                        .map(professionalLanguageMapper::toResponse)
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
                                                                .toUpperCase(Locale.ROOT),
                                                        99
                                                )
                                )
                        )
                        .map(socialLinkMapper::toResponse)
                        .toList();

        List<GalleryImageResponse> galleryImages =
                galleryImageRepository
                        .findBySpecialistProfileIdOrderBySortOrderAscCreatedAtAsc(
                                profile.getId()
                        )
                        .stream()
                        .map(galleryImageMapper::toResponse)
                        .toList();

        return specialistProfileMapper.toResponse(
                profile,
                workTopics,
                techniques,
                languages,
                socialLinks,
                galleryImages

        );
    }

    private void markProfileAsDraft(
            SpecialistProfile profile
    ) {
        profile.setPublicationStatus(PublicationStatus.BORRADOR);
        profile.setRejectionReason(null);
    }

    private void validateProfileReadyToPublish(
            SpecialistProfile profile
    ) {
        List<String> missingRequirements = new ArrayList<>();

        addMissing(missingRequirements, profile.getPhotoUrl(), "foto de perfil");
        addMissing(missingRequirements, profile.getBannerUrl(), "banner");
        addMissing(missingRequirements, profile.getBiography(), "biografía");
        addMissing(missingRequirements, profile.getDescription(), "descripción");
        addMissing(missingRequirements, profile.getWhatsappPhone(), "número de WhatsApp");

        if (!missingRequirements.isEmpty()) {
            throw new BusinessLogicException(
                    "No se puede publicar el perfil. Completa: "
                            + String.join(", ", missingRequirements)
                            + "."
            );
        }
    }

    private void addMissing(List<String> missingRequirements, String value, String label) {
        if (value == null || value.isBlank()) {
            missingRequirements.add(label);
        }
    }

    private String requireText(
            String value,
            String message
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value.trim();
    }

    private String cleanOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
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
