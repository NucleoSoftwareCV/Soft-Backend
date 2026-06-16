package com.hean.consigueventas.oonabe.catalog.mapper;

import com.hean.consigueventas.oonabe.catalog.dto.CityDTO;
import com.hean.consigueventas.oonabe.catalog.entity.City;
import org.springframework.stereotype.Component;

@Component
public class CityMapper {
    public CityDTO toDto(City city) {
        return new CityDTO(city.getId(), city.getName(), city.getProvince(), city.getCountryCode(), city.getIsActive());
    }
}
