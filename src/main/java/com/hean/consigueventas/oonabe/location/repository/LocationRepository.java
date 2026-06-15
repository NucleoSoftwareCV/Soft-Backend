package com.hean.consigueventas.oonabe.location.repository;

import com.hean.consigueventas.oonabe.location.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long>
{
    List<Location> findAllByOrderByNameAsc();

    Optional<Location> findByName(String name);
}
