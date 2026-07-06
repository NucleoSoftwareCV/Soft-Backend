package com.hean.consigueventas.oonabe.managementAdmin.mapper;

import com.hean.consigueventas.oonabe.managementAdmin.dto.PromotionResponseDto;
import com.hean.consigueventas.oonabe.managementAdmin.entity.RolePromotionRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RolePromotionMapper {

    @Mapping(source = "user.id", target = "userId")
    PromotionResponseDto toDto(RolePromotionRequest entity);
}
