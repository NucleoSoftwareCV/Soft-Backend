package com.hean.consigueventas.oonabe.masterdata.mapper;

import com.hean.consigueventas.oonabe.masterdata.dto.Admin.WorkTopicAdminDTO;
import com.hean.consigueventas.oonabe.masterdata.dto.User.WorkTopicPublicDTO;
import com.hean.consigueventas.oonabe.masterdata.entity.WorkTopic;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface WorkTopicMapper {

    WorkTopicPublicDTO toPublicDto(WorkTopic workTopic);

    WorkTopicAdminDTO toAdminDto(WorkTopic workTopic);

    @Mapping(target = "id", ignore = true)
    WorkTopic toEntity(WorkTopicAdminDTO workTopicAdminDTO);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(
            WorkTopicAdminDTO workTopicAdminDTO,
            @MappingTarget WorkTopic workTopic
    );
}