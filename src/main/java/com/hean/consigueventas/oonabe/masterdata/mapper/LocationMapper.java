package com.hean.consigueventas.oonabe.masterdata.mapper;

import com.hean.consigueventas.oonabe.masterdata.dto.request.LocationUpsertRequest;
import com.hean.consigueventas.oonabe.masterdata.dto.response.LocationResponse;
import com.hean.consigueventas.oonabe.masterdata.entity.Location;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "city", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    Location toEntity(
            LocationUpsertRequest request
    );


    @Mapping(target = "cityName", source = "city.name")
    LocationResponse toDto(Location location);
}
