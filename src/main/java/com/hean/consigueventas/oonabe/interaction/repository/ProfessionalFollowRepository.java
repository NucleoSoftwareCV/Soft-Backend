package com.hean.consigueventas.oonabe.interaction.repository;

import com.hean.consigueventas.oonabe.common.enums.ApprovalStatus;
import com.hean.consigueventas.oonabe.common.enums.PublicationStatus;
import com.hean.consigueventas.oonabe.interaction.entity.ProfessionalFollow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProfessionalFollowRepository extends JpaRepository<ProfessionalFollow, Long> {

    Optional<ProfessionalFollow> findByUserIdAndSpecialistProfileId(Long userId, Long specialistProfileId);

    boolean existsByUserIdAndSpecialistProfileId(Long userId, Long specialistProfileId);

    @EntityGraph(attributePaths = "specialistProfile")
    @Query("""
            SELECT follow
            FROM ProfessionalFollow follow
            WHERE follow.user.id = :userId
              AND follow.specialistProfile.approvalStatus = :approvalStatus
              AND follow.specialistProfile.publicationStatus = :publicationStatus
            """)
    Page<ProfessionalFollow> findVisibleByUserId(
            @Param("userId") Long userId,
            @Param("approvalStatus") ApprovalStatus approvalStatus,
            @Param("publicationStatus") PublicationStatus publicationStatus,
            Pageable pageable
    );
}
