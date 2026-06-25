package com.hean.consigueventas.oonabe.masterdata.repository;

import com.hean.consigueventas.oonabe.masterdata.entity.WorkTopic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkTopicRepository extends JpaRepository<WorkTopic, Long> {

    //Busca un tema por nombre con diferenciador de mayusculas y minusculas
    Optional<WorkTopic> findByNameIgnoreCase(String name);

    //Comprueba si el tema ya existe antes de crearlo
    boolean existsByNameIgnoreCase(String name);

    //Trae unicamente los temas activos y los ordena alfabeticamente
    List<WorkTopic> findByActive(boolean active);
}