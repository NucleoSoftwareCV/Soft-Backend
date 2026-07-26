package com.hean.consigueventas.oonabe.profileCliente.repository;

import com.hean.consigueventas.oonabe.profileCliente.entity.ClientInterestCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientInterestCategoryRepository
        extends JpaRepository<ClientInterestCategory, Long> {

    List<ClientInterestCategory> findByClientProfileId(Long clientProfileId);

    boolean existsByClientProfileIdAndCategoryId(
            Long clientProfileId,
            Long categoryId
    );

    void deleteByClientProfileId(Long clientProfileId);
}