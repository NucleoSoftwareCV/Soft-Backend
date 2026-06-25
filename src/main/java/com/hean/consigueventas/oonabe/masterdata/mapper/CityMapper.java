package com.hean.consigueventas.oonabe.masterdata.mapper;

import com.hean.consigueventas.oonabe.masterdata.dto.Admin.CityAdminDTO;
import com.hean.consigueventas.oonabe.masterdata.dto.User.CityPublicDTO;
import com.hean.consigueventas.oonabe.masterdata.entity.City;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CityMapper {

    CityPublicDTO toDto(City city);

    @Mapping(target = "active", source = "isActive")
    CityAdminDTO toAdminDto(City city);
}