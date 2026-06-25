package com.hean.consigueventas.oonabe.masterdata.mapper;

import com.hean.consigueventas.oonabe.masterdata.dto.WorkTopicDTO;
import com.hean.consigueventas.oonabe.masterdata.entity.WorkTopic;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface WorkTopicMapper {

    WorkTopicDTO toDto(WorkTopic workTopic);

    @Mapping(target = "id", ignore = true)
    WorkTopic toEntity(WorkTopicDTO workTopicDTO);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(
            WorkTopicDTO workTopicDTO,
            @MappingTarget WorkTopic workTopic
    );
}