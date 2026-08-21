package com.hean.consigueventas.oonabe.masterdata.repository;

import com.hean.consigueventas.oonabe.masterdata.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CityRepository extends JpaRepository<City, Long> {

    //Filtro público
    List<City> findByIsActiveTrue();

    //Filtro administrativo por estado
    List<City> findByIsActive(Boolean isActive);

    // DataInitializer
    Optional<City> findByNameAndProvince(
            String name,
            String province
    );
}