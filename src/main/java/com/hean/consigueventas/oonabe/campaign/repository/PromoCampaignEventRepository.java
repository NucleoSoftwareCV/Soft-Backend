package com.hean.consigueventas.oonabe.campaign.repository;

import com.hean.consigueventas.oonabe.campaign.domain.CampaignStatus;
import com.hean.consigueventas.oonabe.campaign.entity.PromoCampaign;
import com.hean.consigueventas.oonabe.campaign.entity.PromoCampaignEvent;
import com.hean.consigueventas.oonabe.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface PromoCampaignEventRepository extends JpaRepository<PromoCampaignEvent, Long> {

    interface BadgeRow {
        Long getEventId();

        String getSlug();

        String getBadgeLabel();

        String getTitle();
    }

    /** Distintivos de campañas abiertas para un lote de eventos (una sola consulta por página de tarjetas). */
    @Query("""
            select pce.event.id as eventId, c.slug as slug, c.badgeLabel as badgeLabel, c.title as title
            from PromoCampaignEvent pce join pce.campaign c
            where pce.event.id in :eventIds
              and c.status = :status
              and c.participationStartsAt <= :now and c.participationEndsAt >= :now
            order by c.participationEndsAt asc, c.id asc
            """)
    List<BadgeRow> findOpenBadges(
            @Param("eventIds") Collection<Long> eventIds,
            @Param("status") CampaignStatus status,
            @Param("now") Instant now);

    @Query("""
            select c from PromoCampaignEvent pce join pce.campaign c
            where pce.event.id = :eventId
              and c.status = :status
              and c.participationStartsAt <= :at and c.participationEndsAt >= :at
            order by c.id asc
            """)
    List<PromoCampaign> findOpenCampaignsForEvent(
            @Param("eventId") Long eventId,
            @Param("status") CampaignStatus status,
            @Param("at") Instant at);

    @Query("select pce.event.id from PromoCampaignEvent pce where pce.campaign.id = :campaignId order by pce.event.id")
    List<Long> findEventIdsByCampaignId(@Param("campaignId") Long campaignId);

    @Query("select pce.event from PromoCampaignEvent pce where pce.campaign.id = :campaignId order by pce.event.id")
    List<Event> findEventsByCampaignId(@Param("campaignId") Long campaignId);

    long countByCampaignId(Long campaignId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from PromoCampaignEvent pce where pce.campaign.id = :campaignId")
    void deleteByCampaignId(@Param("campaignId") Long campaignId);
}
