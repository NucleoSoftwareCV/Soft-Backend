package com.hean.consigueventas.oonabe.rewards.Dto.Response;

import com.hean.consigueventas.oonabe.rewards.Enum.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class PointsTransactionsResponse {

    private Long id;
    private TransactionType type; // EARN, REDEEM
    private Integer pointsQuantity;
    private Integer balanceAfter;
    private LocalDateTime createdAt;

    private String userEmail;
}
