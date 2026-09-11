package com.hean.consigueventas.oonabe.profileProfesional.controller;

import com.hean.consigueventas.oonabe.common.enums.ApprovalStatus;
import com.hean.consigueventas.oonabe.common.enums.PublicationStatus;
import com.hean.consigueventas.oonabe.profileProfesional.dto.request.ProfessionalLanguageRequest;
import com.hean.consigueventas.oonabe.profileProfesional.dto.request.ProfessionalSocialLinkRequest;
import com.hean.consigueventas.oonabe.profileProfesional.dto.request.SpecialistProfilePartialUpdateRequest;
import com.hean.consigueventas.oonabe.profileProfesional.dto.request.SpecialistProfileRequest;
import com.hean.consigueventas.oonabe.profileProfesional.dto.response.ProfessionalLanguageResponse;
import com.hean.consigueventas.oonabe.profileProfesional.dto.response.ProfessionalSocialLinkResponse;
import com.hean.consigueventas.oonabe.profileProfesional.dto.response.SpecialistProfileResponse;
import com.hean.consigueventas.oonabe.profileProfesional.service.SpecialistProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/specialist-profiles")
@Tag(
        name = "Perfiles profesionales",
        description = "Gestión de perfiles profesionales, aprobación, publicación y redes sociales."
)
public class SpecialistProfileController {

    private final SpecialistProfileService specialistProfileService;

    public SpecialistProfileController(
            SpecialistProfileService specialistProfileService
    ) {
        this.specialistProfileService = specialistProfileService;
    }

    //Profesional: crear perfil
    @PostMapping
    @PreAuthorize("hasRole('PROFESSIONAL')")
    @Operation(
            summary = "Crear perfil profesional",
            description = "Permite al profesional autenticado crear su propio perfil."
    )
    public SpecialistProfileResponse createProfile(
            Authentication authentication,
            @Valid @RequestBody SpecialistProfileRequest request
    ) {
        return specialistProfileService.createProfile(
                authentication.getName(),
                request
        );
    }

    //Profesional: ver su perfil
    @GetMapping("/me")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    @Operation(
            summary = "Consultar mi perfil profesional",
            description = "Devuelve el perfil asociado al profesional autenticado."
    )
    public SpecialistProfileResponse getMyProfile(
            Authentication authentication
    ) {
        return specialistProfileService.getMyProfile(
                authentication.getName()
        );
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    @Operation(
            summary = "Actualizar todo mi perfil profesional",
            description = "Actualiza todo el perfil profesional. Debes enviar todos los campos obligatorios del perfil."
    )
    public SpecialistProfileResponse updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody SpecialistProfileRequest request
    ) {
        return specialistProfileService.updateMyProfile(
                authentication.getName(),
                request
        );
    }

    //Profesional: actualiza SOLO algunos campos. No es necesario enviar todo el perfil.
    @PatchMapping("/me")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    @Operation(
            summary = "Actualizar parcialmente mi perfil profesional",
            description = "Actualiza solo los campos enviados. Los campos que no se envían se mantienen igual."
    )
    public SpecialistProfileResponse updateMyProfilePartial(
            Authentication authentication,
            @Valid @RequestBody SpecialistProfilePartialUpdateRequest request
    ) {
        return specialistProfileService.updateMyProfilePartial(
                authentication.getName(),
                request
        );
    }

    //Admin: aprueba un perfil profesional
    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Aprobar perfil profesional",
            description = "Permite al administrador aprobar un perfil profesional pendiente."
    )
    public SpecialistProfileResponse approveProfile(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return specialistProfileService.approveProfile(
                id,
                authentication.getName()
        );
    }

    //Admin: rechaza un perfil profesional
    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Rechazar perfil profesional",
            description = "Permite al administrador rechazar un perfil e indicar el motivo."
    )
    public SpecialistProfileResponse rejectProfile(
            @PathVariable Long id,
            @RequestParam String rejectionReason,
            Authentication authentication
    ) {
        return specialistProfileService.rejectProfile(
                id,
                rejectionReason,
                authentication.getName()
        );
    }

    // Profesional: publica su propio perfil
    @PatchMapping("/me/publish")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    @Operation(
            summary = "Publicar mi perfil profesional",
            description = "Publica el perfil del profesional autenticado cuando ya se encuentra aprobado."
    )
    public SpecialistProfileResponse publishMyProfile(
            Authentication authentication
    ) {
        return specialistProfileService.publishMyProfile(
                authentication.getName()
        );
    }

    //Profesional: ocultar su perfil
    @PatchMapping("/me/unpublish")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    @Operation(
            summary = "Ocultar mi perfil profesional",
            description = "Cambia el perfil publicado nuevamente al estado borrador."
    )
    public SpecialistProfileResponse unpublishMyProfile(
            Authentication authentication
    ) {
        return specialistProfileService.unpublishMyProfile(
                authentication.getName()
        );
    }

    //Público: listar perfiles aprobados y publicados
    //page: numero de pagina
    //size: cantidad de elementos por pagina
    //sort:orden en que traen los datos
    //En string poner:
    //id,desc
    //publicName,asc
    //createdAt,desc
    //updatedAt,desc
    @GetMapping
    @Operation(
            summary = "Listar perfiles profesionales públicos",
            description = "Lista perfiles profesionales aprobados y publicados, permitiendo filtrar por categoría."
    )
    public Page<SpecialistProfileResponse> getPublicProfiles(

            @Parameter(
                    description = "Categoría del perfil profesional. Si se omite, lista todas las categorías.",
                    schema = @Schema(
                            allowableValues = {
                                    "PROFESIONALES",
                                    "CENTRO",
                                    "ORGANIZADOR"
                            }
                    )
            )
            @RequestParam(required = false) String profileCategory,

            @Parameter(description = "Busqueda por texto libre en nombre publico o biografia")
            @RequestParam(required = false) String search,

            @ParameterObject
            @PageableDefault(
                    size = 10,
                    sort = "publicName",
                    direction = Sort.Direction.ASC
            ) Pageable pageable
    ) {
        return specialistProfileService.getPublicProfiles(
                profileCategory,
                search,
                pageable
        );
    }

    //approvalStatus = revisión del admin
    //PENDIENTE / APROBADO / RECHAZADO

    //publicationStatus = visibilidad del perfil (profesional)
    //BORRADOR / PUBLICADO

    //Admin: listar perfiles por estado de aprobación y estado de publicación
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Listar perfiles profesionales para administración",
            description = "Permite al administrador listar perfiles profesionales filtrando por estado de aprobación y publicación."
    )
    public Page<SpecialistProfileResponse> getProfilesForAdmin(
            @RequestParam(required = false) ApprovalStatus approvalStatus,
            @RequestParam(required = false) PublicationStatus publicationStatus,

            @ParameterObject
            @PageableDefault(
                    size = 10,
                    sort = "updatedAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return specialistProfileService.getProfilesForAdmin(
                approvalStatus,
                publicationStatus,
                pageable
        );
    }

    //Público: consultar un perfil mediante su slug
    @GetMapping("/slug/{slug}")
    @Operation(
            summary = "Consultar perfil público por slug",
            description = "Devuelve el detalle de un perfil aprobado y publicado mediante su enlace identificador."
    )
    public SpecialistProfileResponse getPublicProfileBySlug(
            @PathVariable String slug
    ) {
        return specialistProfileService.getPublicProfileBySlug(slug);
    }

    //Profesional: registra o actualiza una red social de su perfil
    @PutMapping("/me/social-links")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    @Operation(
            summary = "Registrar o actualizar una red social",
            description = "Permite al profesional agregar una red social o actualizar su enlace existente."
    )
    public ProfessionalSocialLinkResponse saveSocialLink(
            Authentication authentication,
            @Valid @RequestBody ProfessionalSocialLinkRequest request
    ) {
        return specialistProfileService.saveSocialLink(
                authentication.getName(),
                request
        );
    }

    //Profesional: elimina una red social de su perfil
    @DeleteMapping("/me/social-links/{platform}")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    @Operation(
            summary = "Eliminar red social",
            description = "Permite al profesional eliminar una red social registrada."
    )
    public void deleteSocialLink(
            Authentication authentication,

            @Parameter(
                    description = "Plataforma de red social",
                    schema = @Schema(
                            allowableValues = {
                                    "INSTAGRAM",
                                    "YOUTUBE",
                                    "FACEBOOK",
                                    "TIKTOK"
                            }
                    )
            )
            @PathVariable String platform
    ) {
        specialistProfileService.deleteSocialLink(
                authentication.getName(),
                platform
        );
    }

    //Profesional: guardar o actualizar un idioma
    @PostMapping("/me/languages")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    @Operation(
            summary = "Guardar idioma del profesional",
            description = "Permite al profesional registrar o actualizar un idioma en su perfil."
    )
    public ProfessionalLanguageResponse saveLanguage(
            Authentication authentication,
            @Valid @RequestBody ProfessionalLanguageRequest request
    ) {
        return specialistProfileService.saveLanguage(
                authentication.getName(),
                request
        );
    }

    //Profesional: eliminar un idioma
    @DeleteMapping("/me/languages/{languageId}")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    @Operation(
            summary = "Eliminar idioma del profesional",
            description = "Permite al profesional eliminar un idioma registrado en su perfil."
    )
    public void deleteLanguage(
            Authentication authentication,
            @PathVariable Long languageId
    ) {
        specialistProfileService.deleteLanguage(
                authentication.getName(),
                languageId
        );
    }

    //Profesional: sube o reemplaza su foto de perfil
    @PostMapping(
            value = "/me/photo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('PROFESSIONAL')")
    @Operation(
            summary = "Subir foto de perfil",
            description = "Permite al profesional subir o reemplazar su foto de perfil con medidas 512x512."
    )
    public SpecialistProfileResponse uploadProfilePhoto(
            Authentication authentication,
            @RequestParam("file") MultipartFile file
    ) {
        return specialistProfileService.uploadProfilePhoto(
                authentication.getName(),
                file
        );
    }

    //Profesional: sube o reemplaza su banner
    @PostMapping(
            value = "/me/banner",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('PROFESSIONAL')")
    @Operation(
            summary = "Subir banner del perfil",
            description = "Permite al profesional subir o reemplazar su banner con medidas 1248x256."
    )
    public SpecialistProfileResponse uploadBanner(
            Authentication authentication,
            @RequestParam("file") MultipartFile file
    ) {
        return specialistProfileService.uploadBanner(
                authentication.getName(),
                file
        );
    }

    //Profesional: sube una imagen a su galería
    @PostMapping(
            value = "/me/gallery-images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('PROFESSIONAL')")
    @Operation(
            summary = "Subir imagen a la galería",
            description = "Permite al profesional agregar una imagen a la galería de su perfil (máximo 12)."
    )
    public SpecialistProfileResponse uploadGalleryImage(
            Authentication authentication,
            @RequestParam("file") MultipartFile file
    ) {
        return specialistProfileService.uploadGalleryImage(
                authentication.getName(),
                file
        );
    }

    //Profesional: elimina una imagen de su galería
    @DeleteMapping("/me/gallery-images/{imageId}")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    @Operation(
            summary = "Eliminar imagen de la galería",
            description = "Permite al profesional eliminar una imagen de la galería de su perfil."
    )
    public void deleteGalleryImage(
            Authentication authentication,
            @PathVariable Long imageId
    ) {
        specialistProfileService.deleteGalleryImage(
                authentication.getName(),
                imageId
        );
    }
}
