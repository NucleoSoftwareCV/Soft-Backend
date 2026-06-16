package com.hean.consigueventas.oonabe.city.mapper;

import com.hean.consigueventas.oonabe.city.dto.CityDTO;
import com.hean.consigueventas.oonabe.city.entity.City;
import org.springframework.stereotype.Component;

@Component
public class CityMapper {
    public CityDTO toDto(City city) {
        return new CityDTO(city.getId(), city.getName(), city.getProvince(), city.getCountryCode(), city.getIsActive());
    }
}
