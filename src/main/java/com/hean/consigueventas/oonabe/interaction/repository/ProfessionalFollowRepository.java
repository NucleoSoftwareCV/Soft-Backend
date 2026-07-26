package com.hean.consigueventas.oonabe.interaction.repository;

import com.hean.consigueventas.oonabe.interaction.entity.ProfessionalFollow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfessionalFollowRepository
        extends JpaRepository<ProfessionalFollow, Long> {

    Page<ProfessionalFollow> findByClientProfileId(
            Long clientProfileId,
            Pageable pageable
    );

    Optional<ProfessionalFollow> findByClientProfileIdAndSpecialistProfileId(
            Long clientProfileId,
            Long specialistProfileId
    );

    boolean existsByClientProfileIdAndSpecialistProfileId(
            Long clientProfileId,
            Long specialistProfileId
    );

    void deleteByClientProfileIdAndSpecialistProfileId(
            Long clientProfileId,
            Long specialistProfileId
    );

    long countBySpecialistProfileId(Long specialistProfileId);

    long countByClientProfileId(Long clientProfileId);
}