package com.hean.consigueventas.oonabe.oneToOneSession.repository;

import com.hean.consigueventas.oonabe.common.enums.PublicationStatus;
import com.hean.consigueventas.oonabe.oneToOneSession.entity.OneToOneService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OneToOneServiceRepository extends JpaRepository<OneToOneService, Long> {
    List<OneToOneService> findByStatus(PublicationStatus status);
    Page<OneToOneService> findByStatus(PublicationStatus status, Pageable pageable);
    List<OneToOneService> findBySpecialistId(Long specialistId);
    Optional<OneToOneService> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, Long id);
}
