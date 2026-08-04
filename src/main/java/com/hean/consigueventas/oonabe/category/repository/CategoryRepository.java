package com.hean.consigueventas.oonabe.category.repository;

import com.hean.consigueventas.oonabe.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByActiveTrueOrderByNameAsc();

    List<Category> findAllByOrderByNameAsc();

    Optional<Category> findByName(String name);

    Optional<Category> findBySlug(String slug);

    Optional<Category> findByDescription(String description);

    Optional<Category> findBySlugAndActiveTrue(String slug);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
