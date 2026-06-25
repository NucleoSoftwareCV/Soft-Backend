package com.hean.consigueventas.oonabe.masterdata.mapper;

import com.hean.consigueventas.oonabe.masterdata.dto.TechniqueDTO;
import com.hean.consigueventas.oonabe.masterdata.entity.Technique;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TechniqueMapper {

    TechniqueDTO toDto(Technique technique);

    @Mapping(target = "id", ignore = true)
    Technique toEntity(TechniqueDTO techniqueDTO);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(
            TechniqueDTO techniqueDTO,
            @MappingTarget Technique technique
    );
}
