package com.hean.consigueventas.oonabe.profileProfesional.repository;

import com.hean.consigueventas.oonabe.profileProfesional.entity.ProfessionalGalleryImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfessionalGalleryImageRepository
        extends JpaRepository<ProfessionalGalleryImage, Long> {

    List<ProfessionalGalleryImage> findBySpecialistProfileIdOrderBySortOrderAscCreatedAtAsc(
            Long specialistProfileId
    );

    Optional<ProfessionalGalleryImage> findBySpecialistProfileIdAndId(
            Long specialistProfileId,
            Long id
    );

    long countBySpecialistProfileId(Long specialistProfileId);

    void deleteBySpecialistProfileId(Long specialistProfileId);
}
