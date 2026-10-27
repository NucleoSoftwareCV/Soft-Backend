package com.hean.consigueventas.oonabe.event.repository;

import com.hean.consigueventas.oonabe.event.dto.response.EventMapPointResponse;
import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface EventMapRepository extends Repository<EventOccurrence, Long> {

    String VISIBLE_LOCATIONS = """
            from EventOccurrence occurrence
            join occurrence.event event
            join event.specialist specialist
            join specialist.user owner
            join event.category category
            left join event.experienceType experienceType
            join occurrence.location location
            join location.city city
            where event.status = com.hean.consigueventas.oonabe.common.enums.EventStatus.PUBLICADO
              and event.modality <> com.hean.consigueventas.oonabe.common.enums.EventModality.ONLINE
              and specialist.approvalStatus = com.hean.consigueventas.oonabe.common.enums.ApprovalStatus.APROBADO
              and specialist.publicationStatus = com.hean.consigueventas.oonabe.common.enums.PublicationStatus.PUBLICADO
              and owner.status = com.hean.consigueventas.oonabe.common.enums.UserStatus.ACTIVO
              and owner.disabledAt is null
              and category.active = true
              and (experienceType.id is null or experienceType.active = true)
              and occurrence.status = com.hean.consigueventas.oonabe.common.enums.EventOccurrenceStatus.PROGRAMADA
              and occurrence.startsAt > :now
              and occurrence.capacity > occurrence.reservedSpots
              and location.isActive = true
              and city.isActive = true
              and location.latitude between -90 and 90
              and location.longitude between -180 and 180
            """;

    @Query(
            value = """
                    select new com.hean.consigueventas.oonabe.event.dto.response.EventMapPointResponse(
                        occurrence.id, event.id, event.title, event.priceFrom, event.currency,
                        occurrence.startsAt, occurrence.endsAt, location.name,
                        location.address, city.name, location.latitude, location.longitude,
                        event.summary, category.id, category.name, experienceType.id,
                        experienceType.name, specialist.id, specialist.publicName,
                        event.modality, event.isRecurring, event.mood
                    )
                    """ + VISIBLE_LOCATIONS + " order by occurrence.startsAt, occurrence.id",
            countQuery = "select count(occurrence) " + VISIBLE_LOCATIONS
    )
    Page<EventMapPointResponse> findAvailableLocations(
            @Param("now") Instant now,
            Pageable pageable
    );
}
