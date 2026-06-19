package com.hean.consigueventas.oonabe.masterdata.mapper;

import com.hean.consigueventas.oonabe.masterdata.dto.LocationDTO;
import com.hean.consigueventas.oonabe.masterdata.entity.Location;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    LocationDTO toDto(Location location);
}
