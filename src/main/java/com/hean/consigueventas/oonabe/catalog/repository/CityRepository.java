package com.hean.consigueventas.oonabe.catalog.repository;

import com.hean.consigueventas.oonabe.catalog.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CityRepository extends JpaRepository<City, Long> {
    List<City> findByIsActiveTrueOrderByNameAsc();

//    Optional<City> findByName(String name);
    Optional<City> findByNameAndProvince(String name, String province);
}
