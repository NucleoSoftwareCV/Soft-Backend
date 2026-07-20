package com.hean.consigueventas.oonabe.interaction.mapper;

import com.hean.consigueventas.oonabe.interaction.dto.FollowedProfessionalResponse;
import com.hean.consigueventas.oonabe.interaction.entity.ProfessionalFollow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProfessionalFollowMapper {

    @Mapping(target = "id", source = "specialistProfile.id")
    @Mapping(target = "slug", source = "specialistProfile.slug")
    @Mapping(target = "publicName", source = "specialistProfile.publicName")
    @Mapping(target = "profileCategory", source = "specialistProfile.profileCategory")
    @Mapping(target = "photoUrl", source = "specialistProfile.photoUrl")
    FollowedProfessionalResponse toResponse(ProfessionalFollow follow);
}
