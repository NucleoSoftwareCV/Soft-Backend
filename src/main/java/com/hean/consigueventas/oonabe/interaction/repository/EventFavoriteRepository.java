package com.hean.consigueventas.oonabe.interaction.repository;

import com.hean.consigueventas.oonabe.interaction.entity.EventFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
@Repository
public interface EventFavoriteRepository
        extends JpaRepository<EventFavorite, Long> {

    List<EventFavorite> findByClientProfileIdOrderByCreatedAtDesc(
            Long clientProfileId
    );

    Optional<EventFavorite> findByClientProfileIdAndEventId(
            Long clientProfileId,
            Long eventId
    );

    boolean existsByClientProfileIdAndEventId(
            Long clientProfileId,
            Long eventId
    );

    void deleteByClientProfileIdAndEventId(
            Long clientProfileId,
            Long eventId
    );

    long countByClientProfileId(Long clientProfileId);
    @Query("""
        select distinct favorite.event.category.id
        from EventFavorite favorite
        where favorite.clientProfile.id = :clientProfileId
        """)
    List<Long> findFavoriteCategoryIdsByClientProfileId(
            @Param("clientProfileId") Long clientProfileId
    );
}