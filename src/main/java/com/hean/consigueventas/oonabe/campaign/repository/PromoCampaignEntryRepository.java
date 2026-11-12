package com.hean.consigueventas.oonabe.campaign.repository;

import com.hean.consigueventas.oonabe.campaign.domain.CampaignEntryStatus;
import com.hean.consigueventas.oonabe.campaign.entity.PromoCampaignEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface PromoCampaignEntryRepository extends JpaRepository<PromoCampaignEntry, Long> {

    boolean existsByCampaignIdAndBookingId(Long campaignId, Long bookingId);

    @Query("""
            select coalesce(sum(e.ticketCount), 0) from PromoCampaignEntry e
            where e.campaign.id = :campaignId and e.customer.id = :customerId and e.status = :status
            """)
    long sumTickets(
            @Param("campaignId") Long campaignId,
            @Param("customerId") Long customerId,
            @Param("status") CampaignEntryStatus status);

    @Query("select e from PromoCampaignEntry e where e.booking.id in :bookingIds and e.status = :status")
    List<PromoCampaignEntry> findByBookingIdsAndStatus(
            @Param("bookingIds") Collection<Long> bookingIds,
            @Param("status") CampaignEntryStatus status);

    @Query("""
            select e from PromoCampaignEntry e
            join fetch e.booking b join fetch b.occurrence o join fetch o.event
            where e.campaign.id = :campaignId and e.customer.user.id = :userId
            order by e.createdAt desc, e.id desc
            """)
    List<PromoCampaignEntry> findForCustomerUser(@Param("campaignId") Long campaignId, @Param("userId") Long userId);

    @Query("""
            select e from PromoCampaignEntry e
            join fetch e.booking b join fetch b.occurrence o join fetch o.event
            join fetch e.customer c join fetch c.user
            where e.campaign.id = :campaignId and (:status is null or e.status = :status)
            order by e.createdAt asc, e.id asc
            """)
    List<PromoCampaignEntry> findForAdmin(@Param("campaignId") Long campaignId, @Param("status") CampaignEntryStatus status);

    @Query("select distinct e.booking.id from PromoCampaignEntry e where e.booking.id in :bookingIds and e.status = :status")
    List<Long> findBookingIdsWithStatus(
            @Param("bookingIds") Collection<Long> bookingIds,
            @Param("status") CampaignEntryStatus status);

    long countByCampaignId(Long campaignId);

    boolean existsByCampaignId(Long campaignId);

    /** Totales por campaña: [campaignId, participaciones, papeletas válidas, clientes con papeletas válidas]. */
    @Query("""
            select e.campaign.id, count(e),
                   coalesce(sum(case when e.status = :valid then e.ticketCount else 0 end), 0),
                   count(distinct case when e.status = :valid then e.customer.id end)
            from PromoCampaignEntry e
            where e.campaign.id in :campaignIds
            group by e.campaign.id
            """)
    List<Object[]> statsByCampaign(
            @Param("campaignIds") Collection<Long> campaignIds,
            @Param("valid") CampaignEntryStatus valid);

    @Query("""
            select e from PromoCampaignEntry e
            join fetch e.customer c join fetch c.user
            where e.campaign.id = :campaignId and e.status = :status
            order by e.id asc
            """)
    List<PromoCampaignEntry> findForDraw(@Param("campaignId") Long campaignId, @Param("status") CampaignEntryStatus status);
}
