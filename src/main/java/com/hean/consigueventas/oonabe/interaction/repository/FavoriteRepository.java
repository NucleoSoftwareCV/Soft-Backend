package com.hean.consigueventas.oonabe.interaction.repository;

import com.hean.consigueventas.oonabe.common.enums.FavoriteEntityType;
import com.hean.consigueventas.oonabe.interaction.entity.Favorite;
import com.hean.consigueventas.oonabe.interaction.entity.FavoriteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {
    List<Favorite> findByIdCustomerId(Long customerId);

    Optional<Favorite> findByIdCustomerIdAndIdEntityTypeAndIdEntityId(Long customerId, FavoriteEntityType entityType, Long entityId);

    boolean existsByIdCustomerIdAndIdEntityTypeAndIdEntityId(Long customerId, FavoriteEntityType entityType, Long entityId);
}