package com.hean.consigueventas.oonabe.oneToOneSession.mapper;

import com.hean.consigueventas.oonabe.masterdata.entity.Technique;
import com.hean.consigueventas.oonabe.masterdata.entity.WorkTopic;
import com.hean.consigueventas.oonabe.oneToOneSession.dto.request.OneToOneServiceRequest;
import com.hean.consigueventas.oonabe.oneToOneSession.dto.response.OneToOneServiceCardResponse;
import com.hean.consigueventas.oonabe.oneToOneSession.dto.response.OneToOneServiceResponse;
import com.hean.consigueventas.oonabe.oneToOneSession.entity.OneToOneService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface OneToOneServiceMapper {

    @Mapping(target = "specialistId", source = "specialist.id")
    @Mapping(target = "specialistName", source = "specialist.publicName")
    @Mapping(target = "locationId", source = "location.id")
    @Mapping(target = "locationName", source = "location.name")
    @Mapping(target = "workTopics", source = "workTopics", qualifiedByName = "mapWorkTopics")
    @Mapping(target = "techniques", source = "techniques", qualifiedByName = "mapTechniques")

    OneToOneServiceResponse toDto(OneToOneService entity);

    @Mapping(target = "specialistName", source = "specialist.publicName")
    OneToOneServiceCardResponse toCardDto(OneToOneService entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "specialist", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "workTopics", source = "workTopics", ignore = true)
    @Mapping(target = "techniques", source = "techniques", ignore = true)
    OneToOneService toEntity(OneToOneServiceRequest request);

    //Metodo auxiliar para MapStruct
    @Named("mapWorkTopics")
    default Set<String> mapWorkTopics(Set<WorkTopic> workTopics) {
        if (workTopics == null) {
            return Set.of();
        }

        return workTopics.stream()
                .map(WorkTopic::getName)
                .collect(Collectors.toSet());
    }

    @Named("mapTechniques")
    default Set<String> mapTechniques(Set<Technique> techniques) {
        if (techniques == null) {
            return Set.of();
        }

        return techniques.stream()
                .map(Technique::getName)
                .collect(Collectors.toSet());
    }
}
