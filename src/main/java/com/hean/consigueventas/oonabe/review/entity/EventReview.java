package com.hean.consigueventas.oonabe.review.entity;

import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Check;
import java.time.Instant;

@Entity
@Table(name = "event_reviews", uniqueConstraints = {
        @UniqueConstraint(name = "uk_event_reviews_event_user", columnNames = {"event_id", "user_id"})
}, indexes = {
        @Index(name = "idx_event_reviews_event_created", columnList = "event_id, created_at, id")
})
@Check(constraints = "stars between 1 and 5")
@Getter
@Setter
public class EventReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Integer stars;

    @Column(length = 2000)
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
