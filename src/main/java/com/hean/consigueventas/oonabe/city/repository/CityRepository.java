package com.hean.consigueventas.oonabe.city.repository;

import com.hean.consigueventas.oonabe.city.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CityRepository extends JpaRepository<City, Long> {
    List<City> findByIsActiveTrueOrderByNameAsc();

}
