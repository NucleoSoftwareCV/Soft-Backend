package com.hean.consigueventas.oonabe.rewards.Service;

import com.hean.consigueventas.oonabe.booking.entity.EventAttendee;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.rewards.Entity.EarnPointsRules;
import com.hean.consigueventas.oonabe.rewards.Entity.PointsTransactions;
import com.hean.consigueventas.oonabe.rewards.Entity.UserPoints;
import com.hean.consigueventas.oonabe.rewards.Enum.TransactionType;
import com.hean.consigueventas.oonabe.rewards.Enum.UserPointStatus;
import com.hean.consigueventas.oonabe.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttendancePointsService {
    private final EntityManager em;
    private final Clock clock;

    @Transactional(propagation = Propagation.MANDATORY)
    public void credit(EventAttendee attendee, Event event) {
        User buyer = attendee.getRegisteredBy();
        if (buyer == null || event.getExperienceType() == null) {
            return;
        }
        var rule = em.createQuery("""
                select r from EarnPointsRules r
                where r.experienceType.id = :typeId and r.active = true and r.reward > 0
                order by r.id
                """, EarnPointsRules.class)
                .setParameter("typeId", event.getExperienceType().getId())
                .setMaxResults(1).getResultStream().findFirst();
        if (rule.isEmpty()) {
            return;
        }

        // Lock the account even when its points balance has not been created yet.
        em.lock(buyer, LockModeType.PESSIMISTIC_WRITE);
        if (em.createQuery("""
                select count(t) from PointsTransactions t where t.eventAttendee.id = :id and t.type = :type
                """, Long.class).setParameter("id", attendee.getId())
                .setParameter("type", TransactionType.EARN).getSingleResult() > 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now(clock);
        UserPoints balance = em.createQuery("select p from UserPoints p where p.user.id = :id", UserPoints.class)
                .setParameter("id", buyer.getId()).getResultStream().findFirst()
                .orElseGet(() -> {
                    UserPoints account = UserPoints.builder().user(buyer).availablePoints(0)
                            .status(UserPointStatus.ACTIVE).updatedAt(now).build();
                    em.persist(account);
                    return account;
                });
        int earned = rule.get().getReward();
        int available = Math.addExact(balance.getAvailablePoints(), earned);
        balance.setAvailablePoints(available);
        balance.setUpdatedAt(now);
        em.persist(PointsTransactions.builder().userPoints(balance).eventAttendee(attendee).event(event)
                .earnPointsRules(rule.get()).type(TransactionType.EARN).pointsQuantity(earned)
                .balanceAfter(available).createdAt(now).build());
    }
}
