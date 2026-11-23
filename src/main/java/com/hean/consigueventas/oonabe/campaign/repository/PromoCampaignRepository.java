package com.hean.consigueventas.oonabe.campaign.repository;

import com.hean.consigueventas.oonabe.campaign.domain.CampaignStatus;
import com.hean.consigueventas.oonabe.campaign.entity.PromoCampaign;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PromoCampaignRepository extends JpaRepository<PromoCampaign, Long> {

    Optional<PromoCampaign> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    List<PromoCampaign> findAllByOrderByParticipationStartsAtDesc();

    /** Campañas publicadas que todavía no han cerrado (incluye las próximas). */
    @Query("""
            select c from PromoCampaign c
            where c.status = :status and c.participationEndsAt >= :now
            order by case when c.participationStartsAt <= :now then 0 else 1 end,
                     c.participationEndsAt asc, c.participationStartsAt asc, c.id asc
            """)
    List<PromoCampaign> findVisibleNotClosed(@Param("status") CampaignStatus status, @Param("now") Instant now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from PromoCampaign c where c.id = :id")
    Optional<PromoCampaign> findByIdForUpdate(@Param("id") Long id);
}
