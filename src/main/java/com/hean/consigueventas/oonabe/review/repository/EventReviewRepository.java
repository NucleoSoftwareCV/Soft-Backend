package com.hean.consigueventas.oonabe.review.repository;

import com.hean.consigueventas.oonabe.review.entity.EventReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;

public interface EventReviewRepository extends JpaRepository<EventReview, Long> {
    boolean existsByEventIdAndUserId(Long eventId, Long userId);

    @EntityGraph(attributePaths = {"user"})
    Page<EventReview> findByEventId(Long eventId, Pageable pageable);

    @Query("""
        select r.event.id as eventId, avg(r.stars) as averageRating, count(r) as reviewCount
        from EventReview r where r.event.id in :eventIds group by r.event.id
        """)
    List<RatingTotals> summarizeByEventIds(@Param("eventIds") Collection<Long> eventIds);

    // Máximo cuatro autores por evento, en un único lote para todas las tarjetas.
    @Query(value = """
        select preview.event_id as "eventId", preview.username as "authorName",
               preview.avatar_url as "avatarUrl"
        from (
            select r.event_id, u.username, cp.avatar_url,
                   row_number() over (partition by r.event_id order by r.created_at desc, r.id desc) as review_rank
            from event_reviews r
            join users u on u.id = r.user_id
            left join customer_profiles cp on cp.user_id = u.id
            where r.event_id in (:eventIds)
        ) preview
        where preview.review_rank <= 4
        order by preview.event_id, preview.review_rank
        """, nativeQuery = true)
    List<ReviewerPreview> findReviewerPreviews(@Param("eventIds") Collection<Long> eventIds);

    interface ReviewerPreview {
        Long getEventId();
        String getAuthorName();
        String getAvatarUrl();
    }

    interface RatingTotals {
        Long getEventId();
        Double getAverageRating();
        Long getReviewCount();
    }
}
