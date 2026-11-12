package com.hean.consigueventas.oonabe.event.itinerary.repository;

import com.hean.consigueventas.oonabe.event.itinerary.entity.EventItineraryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventItineraryItemRepository
        extends JpaRepository<EventItineraryItem, Long> {
    List<EventItineraryItem> findByEventIdOrderBySortOrderAscStartTimeAscIdAsc(Long eventId);
}
