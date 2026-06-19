package com.hean.consigueventas.oonabe.masterdata.mapper;

import com.hean.consigueventas.oonabe.catalog.dto.response.CityResponse;
import com.hean.consigueventas.oonabe.masterdata.entity.City;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CityMapper {

    @Mapping(target = "active", source = "isActive")
    CityResponse toDto(City city);
}
