package com.hean.consigueventas.oonabe.event.repository;

import com.hean.consigueventas.oonabe.event.entity.EventImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EventImageRepository extends JpaRepository<EventImage, Long> {

    @Query("""
            select image
            from EventImage image
            where image.event.id in :eventIds
            order by image.event.id,
                     case when image.cover = true then 0 else 1 end,
                     image.sortOrder,
                     image.id
            """)
    List<EventImage> findOrderedCandidatesByEventIds(@Param("eventIds") Collection<Long> eventIds);

    Optional<EventImage> findFirstByEventIdOrderByCoverDescSortOrderAscIdAsc(Long eventId);

    List<EventImage> findByEventIdOrderBySortOrderAscIdAsc(Long eventId);

    long countByEventId(Long eventId);
}
