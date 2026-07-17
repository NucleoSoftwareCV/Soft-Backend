package com.hean.consigueventas.oonabe.profileProfesional.mapper;

import com.hean.consigueventas.oonabe.profileProfesional.dto.response.ProfessionalImageResponse;
import com.hean.consigueventas.oonabe.profileProfesional.entity.ProfessionalImage;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfessionalImageMapper {

    ProfessionalImageResponse toResponse(ProfessionalImage image);
}