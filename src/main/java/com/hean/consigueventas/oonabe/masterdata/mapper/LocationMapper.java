package com.hean.consigueventas.oonabe.masterdata.mapper;

import com.hean.consigueventas.oonabe.location.dto.response.LocationResponse;
import com.hean.consigueventas.oonabe.masterdata.entity.Location;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    LocationResponse toDto(Location location);
}
