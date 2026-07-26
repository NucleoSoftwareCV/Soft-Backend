package com.hean.consigueventas.oonabe.profileProfesional.mapper;

import com.hean.consigueventas.oonabe.profileProfesional.dto.response.ProfessionalLanguageResponse;
import com.hean.consigueventas.oonabe.profileProfesional.entity.ProfessionalLanguage;
import org.springframework.stereotype.Component;

@Component
public class ProfessionalLanguageMapper {

    public ProfessionalLanguageResponse toResponse(
            ProfessionalLanguage professionalLanguage
    ) {
        return new ProfessionalLanguageResponse(
                professionalLanguage.getId(),
                professionalLanguage.getLanguageName()
        );
    }
}