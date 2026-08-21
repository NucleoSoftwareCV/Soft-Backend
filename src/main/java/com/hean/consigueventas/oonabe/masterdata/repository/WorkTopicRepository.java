package com.hean.consigueventas.oonabe.masterdata.repository;

import com.hean.consigueventas.oonabe.masterdata.entity.WorkTopic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkTopicRepository extends JpaRepository<WorkTopic, Long> {

    // DataInitializer
    // Busca sin diferenciar mayúsculas y minúsculas
    Optional<WorkTopic> findByNameIgnoreCase(String name);

    // Búsqueda pública: solo encuentra temas activos
    Optional<WorkTopic> findByNameIgnoreCaseAndActiveTrue(String name);

    // Comprueba duplicados al crear
    boolean existsByNameIgnoreCase(String name);

    // Comprueba duplicados al actualizar, ignorando el registro actual
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    // Obtiene temas según su estado
    List<WorkTopic> findByActive(boolean active);
}