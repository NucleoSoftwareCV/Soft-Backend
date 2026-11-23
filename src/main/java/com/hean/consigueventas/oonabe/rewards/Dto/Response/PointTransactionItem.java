package com.hean.consigueventas.oonabe.rewards.Dto.Response;

import com.hean.consigueventas.oonabe.rewards.Enum.TransactionType;
import java.time.LocalDateTime;

public record PointTransactionItem(Long id, TransactionType type, Integer pointsQuantity,
                                   Integer balanceAfter, LocalDateTime createdAt) {
}
