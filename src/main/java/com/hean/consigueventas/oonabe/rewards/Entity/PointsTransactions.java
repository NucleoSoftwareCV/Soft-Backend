package com.hean.consigueventas.oonabe.rewards.Entity;


import com.hean.consigueventas.oonabe.booking.entity.EventAttendee;
import com.hean.consigueventas.oonabe.booking.entity.EventBooking;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.rewards.Enum.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "points_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PointsTransactions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_points_id", nullable = false)
    private UserPoints userPoints;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_attendee_id")
    private EventAttendee eventAttendee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "points_rules_id", nullable = false)
    private EarnPointsRules earnPointsRules;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type; // EARN, REDEEM

    @Column(name = "points_quantity", nullable = false)
    private Integer pointsQuantity;

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
