package com.hean.consigueventas.oonabe.masterdata.mapper;

import com.hean.consigueventas.oonabe.masterdata.dto.Admin.TechniqueAdminDTO;
import com.hean.consigueventas.oonabe.masterdata.dto.User.TechniquePublicDTO;
import com.hean.consigueventas.oonabe.masterdata.entity.Technique;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TechniqueMapper {

    TechniquePublicDTO toPublicDto(Technique technique);

    TechniqueAdminDTO toAdminDto(Technique technique);

    @Mapping(target = "id", ignore = true)
    Technique toEntity(TechniqueAdminDTO techniqueAdminDTO);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(
            TechniqueAdminDTO techniqueAdminDTO,
            @MappingTarget Technique technique
    );
}