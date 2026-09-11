package com.hean.consigueventas.oonabe.professionalApplication.mapper;

import com.hean.consigueventas.oonabe.professionalApplication.dto.response.ProfessionalApplicationResponse;
import com.hean.consigueventas.oonabe.professionalApplication.entity.ProfessionalApplication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProfessionalApplicationMapper {

    @Mapping(
            target = "userId",
            source = "user.id"
    )
    @Mapping(
            target = "evaluatedById",
            source = "evaluatedBy.id"
    )
    ProfessionalApplicationResponse toResponse(
            ProfessionalApplication application
    );
}