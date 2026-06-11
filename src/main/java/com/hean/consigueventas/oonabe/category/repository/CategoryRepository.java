package com.hean.consigueventas.oonabe.category.repository;

import com.hean.consigueventas.oonabe.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByActiveTrueOrderByNameAsc();

    Optional<Category> findByName(String name);
}
