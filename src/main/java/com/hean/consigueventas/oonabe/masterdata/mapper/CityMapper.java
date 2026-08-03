package com.hean.consigueventas.oonabe.masterdata.mapper;

import com.hean.consigueventas.oonabe.masterdata.dto.Admin.CityAdminDTO;
import com.hean.consigueventas.oonabe.masterdata.dto.User.CityPublicDTO;
import com.hean.consigueventas.oonabe.masterdata.entity.City;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CityMapper {

    @Mapping(target = "active", source = "isActive")
    CityPublicDTO toDto(City city);

    @Mapping(target = "active", source = "isActive")
    CityAdminDTO toAdminDto(City city);

    @Mapping(target = "isActive", source = "active")
    @Mapping(target = "id", ignore = true)
    City toEntity(CityAdminDTO cityAdminDTO);

    @Mapping(target = "isActive", source = "active")
    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(
            CityAdminDTO cityAdminDTO,
            @MappingTarget City city
    );
}