package com.hean.consigueventas.oonabe.masterdata.repository;

import com.hean.consigueventas.oonabe.masterdata.entity.CityInterest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityInterestRepository extends JpaRepository<CityInterest, Long> {

    // Evita registrar el mismo interes dos veces para la misma ciudad y email normalizado
    boolean existsByCityIdAndEmailIgnoreCase(Long cityId, String email);
}
