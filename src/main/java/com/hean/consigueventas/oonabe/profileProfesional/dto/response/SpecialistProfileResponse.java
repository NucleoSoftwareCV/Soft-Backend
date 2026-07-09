package com.hean.consigueventas.oonabe.profileProfesional.dto.response;

import com.hean.consigueventas.oonabe.common.enums.ApprovalStatus;
import com.hean.consigueventas.oonabe.common.enums.PublicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Schema(description = "Respuesta con los datos del perfil profesional")
public record SpecialistProfileResponse(

        @Schema(description = "ID del perfil")
        Long id,

        @Schema(description = "ID del usuario relacionado")
        Long userId,

        @Schema(description = "Identificador público del perfil")
        String slug,

        @Schema(description = "Nombre público del perfil")
        String publicName,

        @Schema(description = "Categoría del perfil")
        String profileCategory,

        @Schema(description = "Biografía o descripción del perfil")
        String biography,

        @Schema(description = "URL de la foto principal")
        String photoUrl,

        @Schema(description = "Número de WhatsApp")
        String whatsappPhone,

        @Schema(description = "Número celular")
        String phoneNumber,

        @Schema(description = "Correo público")
        String publicEmail,

        @Schema(description = "Página web")
        String website,

        @Schema(description = "Estado de aprobación")
        ApprovalStatus approvalStatus,

        @Schema(description = "Estado de publicación")
        PublicationStatus publicationStatus,

        @Schema(description = "ID del usuario que aprobó el perfil")
        Long approvedById,

        @Schema(description = "Fecha de aprobación")
        Instant approvedAt,

        @Schema(description = "Motivo de rechazo")
        String rejectionReason,

        @Schema(description = "Fecha de creación")
        Instant createdAt,

        @Schema(description = "Fecha de actualización")
        Instant updatedAt,

        @Schema(description = "Temas de trabajo asociados")
        Set<String> workTopics,

        @Schema(description = "Técnicas asociadas")
        Set<String> techniques,

        @Schema(description = "Banner e imágenes de la galería")
        List<ProfessionalImageResponse> images,

        @Schema(description = "Redes sociales del perfil")
        List<ProfessionalSocialLinkResponse> socialLinks
) {
}