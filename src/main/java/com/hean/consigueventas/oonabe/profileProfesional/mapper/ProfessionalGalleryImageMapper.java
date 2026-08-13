package com.hean.consigueventas.oonabe.profileProfesional.mapper;

import com.hean.consigueventas.oonabe.profileProfesional.dto.response.GalleryImageResponse;
import com.hean.consigueventas.oonabe.profileProfesional.entity.ProfessionalGalleryImage;
import org.springframework.stereotype.Component;

@Component
public class ProfessionalGalleryImageMapper {

    public GalleryImageResponse toResponse(
            ProfessionalGalleryImage image
    ) {
        return new GalleryImageResponse(
                image.getId(),
                image.getImageUrl(),
                image.getSortOrder()
        );
    }
}
