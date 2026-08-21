package com.hean.consigueventas.oonabe.experienceType.repository;

import com.hean.consigueventas.oonabe.experienceType.entity.ExperienceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExperienceTypeRepository extends JpaRepository<ExperienceType, Long> {
    List<ExperienceType> findByActiveTrueOrderByNameAsc();
    List<ExperienceType> findAllByOrderByNameAsc();
    Optional<ExperienceType> findBySlug(String slug);
    Optional<ExperienceType> findBySlugAndActiveTrue(String slug);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
