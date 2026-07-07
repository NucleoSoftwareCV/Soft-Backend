package com.hean.consigueventas.oonabe.oneToOneSession.repository;

import com.hean.consigueventas.oonabe.oneToOneSession.entity.OneToOneService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OneToOneServiceRepository extends JpaRepository<OneToOneService, Long>, JpaSpecificationExecutor<OneToOneService> {

    List<OneToOneService> findBySpecialistId(Long specialistId);

    Optional<OneToOneService> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}
