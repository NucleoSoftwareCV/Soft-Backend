package com.hean.consigueventas.oonabe.rewards.Service;

import com.hean.consigueventas.oonabe.booking.dto.Response.EventIdAndUserIdResponse;
import com.hean.consigueventas.oonabe.booking.entity.EventAttendee;
import com.hean.consigueventas.oonabe.booking.entity.EventBooking;
import com.hean.consigueventas.oonabe.common.enums.AttendanceStatus;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.rewards.Dto.Request.EarnPointsRequest;
import com.hean.consigueventas.oonabe.rewards.Dto.Request.RedeemPointsRequest;
import com.hean.consigueventas.oonabe.rewards.Dto.Response.EarnPointsResponse;
import com.hean.consigueventas.oonabe.rewards.Entity.EarnPointsRules;
import com.hean.consigueventas.oonabe.rewards.Entity.PointsTransactions;
import com.hean.consigueventas.oonabe.rewards.Entity.UserPoints;
import com.hean.consigueventas.oonabe.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RewardsService {

    private final FinderService finderService;
    private final UserPointsService userPointsService;
    private final PointsTransactionsService pointsTransactionsService;
    private final EarnPointsRuleService earnPointsRuleService;
    private final RedeemPointsRuleService redeemPointsRuleService;

    // ---------- Consultas ----------

    public Integer getPointsByUserEmail(String userEmail) {
        return userPointsService.getAvailablePointsByUserEmail(userEmail);
    }


    /** Get redeem transactions of user with type=redeem, not available needs dtoResponse*/
    public List<PointsTransactions> getRedemptionsByUserEmail(String userEmail) {
        return pointsTransactionsService.getRedemptionsByUserEmailNType(userEmail);
    }

    // ---------- Ganar puntos ----------

    @Transactional
    public EarnPointsResponse earnPoints(EarnPointsRequest request) {
        // 1. Use eventAttendeeId to find the user and event
        EventIdAndUserIdResponse eventIdAndUserIdResponse = finderService.findEventIdAndUserId(request.getEventAttendanceId());
        Long eventId = eventIdAndUserIdResponse.getEventId();
        Long userId = eventIdAndUserIdResponse.getUserId();
        // 2. Find Event n User
        Event event = finderService.findEventById(eventId);
        User user = finderService.findUserById(userId);

        // 3. Get or create user_points
        UserPoints userPoints = userPointsService.getOrCreateUserPoints(user);

        // 4. Find eventAttendance
        EventAttendee eventAttendee = finderService.findEventAttendeeById(request.getEventAttendanceId());

        // 5. Validate status attendee
        if (eventAttendee.getAttendanceStatus() != AttendanceStatus.ASISTIO) {
            return new EarnPointsResponse("Attendance status is not valid");
        }

        // 6. Get active rule
        EarnPointsRules rule = earnPointsRuleService
                .getActiveRuleByExperienceTypeId(event.getExperienceType().getId());

        // 7. Reward
        Integer pointsEarned = rule.getReward();

        // 8. Add points
        int newBalance = userPointsService.addPoints(userPoints, pointsEarned);

        // 9. Register transaction EARN
        pointsTransactionsService.createEarnTransaction(
                userPoints, eventAttendee, event, rule, pointsEarned, newBalance);

        // 10. Answer
        return new EarnPointsResponse("Points were correctly earned");
    }

    // ---------- Canjear puntos ----------

    @Transactional
    public BigDecimal getRedemptionByPoints(RedeemPointsRequest request) {
        User user = finderService.findUserById(request.getUserId());

        Integer points = userPointsService.getAvailablePointsByUserEmail(user.getEmail());
        Integer redeemPoints = request.getRedeemPoints();

        if (redeemPoints > 0 && redeemPoints > points) {
            throw new RuntimeException("Insufficient points");
        }

        Event event = finderService.findEventById(request.getEventId());
        return redeemPointsRuleService.calculateRedemptionAmount(
                event.getExperienceType().getId(), redeemPoints);
    }


    @Transactional
    public String redeemPoints(RedeemPointsRequest request) {
        Integer redeemPoints = request.getRedeemPoints();
        if (redeemPoints == null || redeemPoints <= 0) {
            throw new IllegalArgumentException("Redeem points must be greater than cero");
        }

        User user = finderService.findUserById(request.getUserId());
        Event event = finderService.findEventById(request.getEventId());
        UserPoints userPoints = userPointsService.findByUserEmail(user.getEmail());
        EarnPointsRules rule = earnPointsRuleService.findRuleById(event.getExperienceType().getId());

        int availablePoints = userPoints.getAvailablePoints();
        if (redeemPoints > availablePoints) {
            throw new IllegalArgumentException("Redeem points should not be greater than available points");
        }

        int newBalance = userPointsService.subtractPoints(userPoints, redeemPoints);

        pointsTransactionsService.createRedeemTransaction(
                userPoints, null, event, rule, redeemPoints, newBalance);

        return "Points transactions created, points were redeemed";
    }
}
