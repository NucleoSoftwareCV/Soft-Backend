package com.hean.consigueventas.oonabe.location.mapper;

import com.hean.consigueventas.oonabe.location.dto.LocationDTO;
import com.hean.consigueventas.oonabe.location.entity.Location;
import org.springframework.stereotype.Component;

@Component
public class LocationMapper {
    public LocationDTO toDto(Location location) {
        return new LocationDTO(location.getId(), location.getName(), location.getAddress(), location.getReference(), location.getIsActive()
        );
    }

}
