package com.hean.consigueventas.oonabe.masterdata.repository;

import com.hean.consigueventas.oonabe.masterdata.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long>
{
    List<Location> findAllByOrderByNameAsc();

    List<Location> findByIsActiveTrueOrderByNameAsc();

    Optional<Location> findByName(String name);
}
