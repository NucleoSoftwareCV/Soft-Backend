package com.hean.consigueventas.oonabe.location.mapper;

import com.hean.consigueventas.oonabe.location.dto.response.LocationResponse;
import com.hean.consigueventas.oonabe.location.entity.Location;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    LocationResponse toDto(Location location);
}
