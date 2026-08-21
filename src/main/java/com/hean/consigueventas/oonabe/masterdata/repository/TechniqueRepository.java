package com.hean.consigueventas.oonabe.masterdata.repository;

import com.hean.consigueventas.oonabe.masterdata.entity.Technique;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TechniqueRepository extends JpaRepository<Technique, Long> {

    //DataInitializer
    //Busca una tecnica por nombre con diferenciador de mayusculas y minusculas
    Optional<Technique> findByNameIgnoreCase(String name);

    // Trae técnicas por estado
    List<Technique> findByActive(boolean active);

    //Trae unicamente las tecnicas activos
    Optional<Technique> findByNameIgnoreCaseAndActiveTrue(String name);

    //Comprueba si la tecnica ya existe antes de crearlo
    boolean existsByNameIgnoreCase(String name);

    // Comprueba si existe tecnicas duplicadas al actualizar, ignorando el mismo registro
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

}
