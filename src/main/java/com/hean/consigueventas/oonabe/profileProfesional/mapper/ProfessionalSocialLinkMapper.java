package com.hean.consigueventas.oonabe.profileProfesional.mapper;

import com.hean.consigueventas.oonabe.profileProfesional.dto.response.ProfessionalSocialLinkResponse;
import com.hean.consigueventas.oonabe.profileProfesional.entity.ProfessionalSocialLink;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfessionalSocialLinkMapper {

    ProfessionalSocialLinkResponse toResponse(
            ProfessionalSocialLink socialLink
    );
}