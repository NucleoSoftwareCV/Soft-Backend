package com.hean.consigueventas.oonabe.oneToOneSession.repository;

import com.hean.consigueventas.oonabe.oneToOneSession.entity.OneToOneService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OneToOneServiceRepository extends JpaRepository<OneToOneService, Long>, JpaSpecificationExecutor<OneToOneService> {

    @EntityGraph(attributePaths = {"specialist", "location", "workTopics", "techniques"})
    List<OneToOneService> findBySpecialistIdOrderByCreatedAtDesc(Long specialistId);

    @Override
    @EntityGraph(attributePaths = {"specialist"})
    Page<OneToOneService> findAll(Specification<OneToOneService> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"specialist", "location", "workTopics", "techniques"})
    Optional<OneToOneService> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}
