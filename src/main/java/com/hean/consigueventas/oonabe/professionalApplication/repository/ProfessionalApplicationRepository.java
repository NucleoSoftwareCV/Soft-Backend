package com.hean.consigueventas.oonabe.professionalApplication.repository;

import com.hean.consigueventas.oonabe.professionalApplication.entity.ProfessionalApplication;
import com.hean.consigueventas.oonabe.professionalApplication.enums.ProfessionalApplicationStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProfessionalApplicationRepository
        extends JpaRepository<ProfessionalApplication, Long> {

    @EntityGraph(attributePaths = {"user", "evaluatedBy"})
    Optional<ProfessionalApplication> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "evaluatedBy"})
    Optional<ProfessionalApplication> findByEmail(String email);

    @Override
    @EntityGraph(attributePaths = {"user", "evaluatedBy"})
    Optional<ProfessionalApplication> findById(Long id);

    boolean existsByEmailAndStatus(
            String email,
            ProfessionalApplicationStatus status
    );

    @EntityGraph(attributePaths = {"user", "evaluatedBy"})
    @Query("""
        select application
        from ProfessionalApplication application
        where (:status is null or application.status = :status)
    """)
    Page<ProfessionalApplication> findForAdmin(
            @Param("status") ProfessionalApplicationStatus status,
            Pageable pageable
    );
}