package com.hean.consigueventas.oonabe.rewards.Service;

import com.hean.consigueventas.oonabe.booking.entity.EventAttendee;
import com.hean.consigueventas.oonabe.booking.entity.EventBooking;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.rewards.Dto.Response.PointsTransactionsResponse;
import com.hean.consigueventas.oonabe.rewards.Entity.EarnPointsRules;
import com.hean.consigueventas.oonabe.rewards.Entity.PointsTransactions;
import com.hean.consigueventas.oonabe.rewards.Entity.UserPoints;
import com.hean.consigueventas.oonabe.rewards.Enum.TransactionType;
import com.hean.consigueventas.oonabe.rewards.Mapper.PointsTransactionsMapper;
import com.hean.consigueventas.oonabe.rewards.Repository.PointsTransactionsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PointsTransactionsService {

    private final PointsTransactionsRepository pointsTransactionsRepository;
    private final PointsTransactionsMapper pointsTransactionsMapper;

    public List<PointsTransactionsResponse> getTransactionsByUserEmail(String userEmail) {
        List<PointsTransactions> pointsTransactions = pointsTransactionsRepository.findByUserPointsUserEmail(userEmail);
        return pointsTransactionsMapper.toResponse(pointsTransactions);
    }

    public List<PointsTransactions> getRedemptionsByUserEmailNType(String userEmail) {
        return pointsTransactionsRepository.findByUserPointsUserEmailAndType(userEmail, TransactionType.REDEEM);
    }

    public PointsTransactions createEarnTransaction(UserPoints userPoints, EventAttendee eventAttendee, Event event,
                                                    EarnPointsRules rule, int pointsQuantity, int balanceAfter) {
        PointsTransactions transaction = PointsTransactions.builder()
                .userPoints(userPoints)
                .eventAttendee(eventAttendee)
                .event(event)
                .earnPointsRules(rule)
                .type(TransactionType.EARN)
                .pointsQuantity(pointsQuantity)
                .balanceAfter(balanceAfter)
                .createdAt(LocalDateTime.now())
                .build();
        return pointsTransactionsRepository.save(transaction);
    }

    public PointsTransactions createRedeemTransaction(UserPoints userPoints, EventAttendee eventAttendee, Event event,
                                                      EarnPointsRules rule, int pointsQuantity, int balanceAfter) {
        PointsTransactions transaction = PointsTransactions.builder()
                .userPoints(userPoints)
                .eventAttendee(eventAttendee)
                .event(event)
                .earnPointsRules(rule)
                .type(TransactionType.REDEEM)
                .pointsQuantity(pointsQuantity)
                .balanceAfter(balanceAfter)
                .createdAt(LocalDateTime.now())
                .build();
        return pointsTransactionsRepository.save(transaction);
    }
}
