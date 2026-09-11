package com.hean.consigueventas.oonabe.professionalApplication.dto.response;

import com.hean.consigueventas.oonabe.professionalApplication.enums.ProfessionalApplicationStatus;
import com.hean.consigueventas.oonabe.professionalApplication.enums.ProfessionalType;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Estado y datos de una solicitud profesional")
public record ProfessionalApplicationResponse(

        Long id, 

      
        Long userId,

        String fullName,

        String email,

        String city,
        ProfessionalType professionalType,

        String whatsappPhone,

        String motivation,

        ProfessionalApplicationStatus status,

        Long evaluatedById,

        Instant evaluatedAt,

        String rejectionReason,

        Instant createdAt,

        Instant updatedAt

) {
}