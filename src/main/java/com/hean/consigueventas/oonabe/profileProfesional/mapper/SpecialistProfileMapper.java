package com.hean.consigueventas.oonabe.profileProfesional.mapper;

import com.hean.consigueventas.oonabe.profileProfesional.dto.request.SpecialistProfileRequest;
import com.hean.consigueventas.oonabe.profileProfesional.dto.response.GalleryImageResponse;
import com.hean.consigueventas.oonabe.profileProfesional.dto.response.ProfessionalLanguageResponse;
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
    @Mapping(target = "photoUrl", ignore = true)
    @Mapping(target = "bannerUrl", ignore = true)
    @Mapping(target = "approvalStatus", ignore = true)
    @Mapping(target = "publicationStatus", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "showUpcomingEvents", ignore = true)
    @Mapping(target = "showOneToOneSessions", ignore = true)
    @Mapping(target = "showGallery", ignore = true)
    SpecialistProfile toEntity(SpecialistProfileRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "photoUrl", ignore = true)
    @Mapping(target = "bannerUrl", ignore = true)
    @Mapping(target = "approvalStatus", ignore = true)
    @Mapping(target = "publicationStatus", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "showUpcomingEvents", ignore = true)
    @Mapping(target = "showOneToOneSessions", ignore = true)
    @Mapping(target = "showGallery", ignore = true)
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
    @Mapping(target = "description", source = "profile.description")    @Mapping(target = "photoUrl", source = "profile.photoUrl")
    @Mapping(target = "bannerUrl", source = "profile.bannerUrl")
    @Mapping(target = "whatsappPhone", source = "profile.whatsappPhone")
    @Mapping(target = "phoneNumber", source = "profile.phoneNumber")
    @Mapping(target = "publicEmail", source = "profile.publicEmail")
    @Mapping(target = "website", source = "profile.website")
    @Mapping(target = "approvalStatus", source = "profile.approvalStatus")
    @Mapping(target = "publicationStatus", source = "profile.publicationStatus")
    @Mapping(target = "approvedById", source = "profile.approvedBy.id")
    @Mapping(target = "approvedAt", source = "profile.approvedAt")
    @Mapping(target = "rejectionReason", source = "profile.rejectionReason")
    @Mapping(target = "createdAt", source = "profile.createdAt")
    @Mapping(target = "updatedAt", source = "profile.updatedAt")
    @Mapping(target = "workTopics", source = "workTopics")
    @Mapping(target = "techniques", source = "techniques")
    @Mapping(target = "languages", source = "languages")
    @Mapping(target = "socialLinks", source = "socialLinks")
    @Mapping(target = "galleryImages", source = "galleryImages")
    @Mapping(target = "showUpcomingEvents", source = "profile.showUpcomingEvents")
    @Mapping(target = "showOneToOneSessions", source = "profile.showOneToOneSessions")
    @Mapping(target = "showGallery", source = "profile.showGallery")
    SpecialistProfileResponse toResponse(
            SpecialistProfile profile,
            Set<String> workTopics,
            Set<String> techniques,
            List<ProfessionalLanguageResponse> languages,
            List<ProfessionalSocialLinkResponse> socialLinks,
            List<GalleryImageResponse> galleryImages
    );
}