package com.hean.consigueventas.oonabe.profileProfesional.mapper;

import com.hean.consigueventas.oonabe.profileProfesional.dto.request.SpecialistProfileRequest;
import com.hean.consigueventas.oonabe.profileProfesional.dto.response.ProfessionalImageResponse;
import com.hean.consigueventas.oonabe.profileProfesional.dto.response.ProfessionalSocialLinkResponse;
import com.hean.consigueventas.oonabe.profileProfesional.dto.response.SpecialistProfileResponse;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface SpecialistProfileMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "approvalStatus", ignore = true)
    @Mapping(target = "publicationStatus", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SpecialistProfile toEntity(SpecialistProfileRequest request);

    @BeanMapping(
            nullValuePropertyMappingStrategy =
                    NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "approvalStatus", ignore = true)
    @Mapping(target = "publicationStatus", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(
            SpecialistProfileRequest request,
            @MappingTarget SpecialistProfile profile
    );

    @Mapping(target = "id", source = "profile.id")
    @Mapping(target = "userId", source = "profile.user.id")
    @Mapping(target = "slug", source = "profile.slug")
    @Mapping(target = "publicName", source = "profile.publicName")
    @Mapping(target = "profileCategory", source = "profile.profileCategory")
    @Mapping(target = "biography", source = "profile.biography")
    @Mapping(target = "photoUrl", source = "profile.photoUrl")
    @Mapping(target = "whatsappPhone", source = "profile.whatsappPhone")
    @Mapping(target = "phoneNumber", source = "profile.phoneNumber")
    @Mapping(target = "publicEmail", source = "profile.publicEmail")
    @Mapping(target = "website", source = "profile.website")
    @Mapping(target = "approvalStatus", source = "profile.approvalStatus")
    @Mapping(
            target = "publicationStatus",
            source = "profile.publicationStatus"
    )
    @Mapping(target = "approvedById", source = "profile.approvedBy.id")
    @Mapping(target = "approvedAt", source = "profile.approvedAt")
    @Mapping(
            target = "rejectionReason",
            source = "profile.rejectionReason"
    )
    @Mapping(target = "createdAt", source = "profile.createdAt")
    @Mapping(target = "updatedAt", source = "profile.updatedAt")
    @Mapping(target = "workTopics", source = "workTopics")
    @Mapping(target = "techniques", source = "techniques")
    @Mapping(target = "images", source = "images")
    @Mapping(target = "socialLinks", source = "socialLinks")
    SpecialistProfileResponse toResponse(
            SpecialistProfile profile,
            Set<String> workTopics,
            Set<String> techniques,
            List<ProfessionalImageResponse> images,
            List<ProfessionalSocialLinkResponse> socialLinks
    );
}