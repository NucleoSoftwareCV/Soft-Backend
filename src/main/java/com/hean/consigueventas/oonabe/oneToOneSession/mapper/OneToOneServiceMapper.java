package com.hean.consigueventas.oonabe.oneToOneSession.mapper;

import com.hean.consigueventas.oonabe.oneToOneSession.dto.request.OneToOneServiceRequest;
import com.hean.consigueventas.oonabe.oneToOneSession.dto.response.OneToOneServiceCardResponse;
import com.hean.consigueventas.oonabe.oneToOneSession.dto.response.OneToOneServiceResponse;
import com.hean.consigueventas.oonabe.oneToOneSession.entity.OneToOneService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OneToOneServiceMapper {

    @Mapping(target = "specialistId", source = "specialist.id")
    @Mapping(target = "specialistName", source = "specialist.publicName")
    @Mapping(target = "locationId", source = "location.id")
    @Mapping(target = "locationName", source = "location.name")
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
    OneToOneService toEntity(OneToOneServiceRequest request);
}
