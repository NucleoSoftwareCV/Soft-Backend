package com.hean.consigueventas.oonabe.support.controller;

import com.hean.consigueventas.oonabe.common.dto.response.PagedResponse;
import com.hean.consigueventas.oonabe.rewards.Enum.TransactionType;
import com.hean.consigueventas.oonabe.rewards.Repository.PointsTransactionsRepository;
import com.hean.consigueventas.oonabe.rewards.Repository.UserPointsRepository;
import com.hean.consigueventas.oonabe.support.enums.TicketCategory;
import com.hean.consigueventas.oonabe.support.service.SupportAccess;
import com.hean.consigueventas.oonabe.support.service.SupportTicketService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/support/admin/tickets")
public class SupportRewardsController {
    private final SupportAccess access;
    private final SupportTicketService tickets;
    private final UserPointsRepository points;
    private final PointsTransactionsRepository transactions;

    @GetMapping("/{id}/rewards")
    @Transactional(readOnly = true)
    public RewardSummary rewards(@PathVariable Long id, @RequestParam(defaultValue = "0") int page) {
        access.admin();
        var ticket = tickets.read(id);
        var category = ticket.getCategory();
        if (category != TicketCategory.REWARDS && category != TicketCategory.CANJES) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "El ticket no corresponde a puntos o canjes");
        }
        Long userId = ticket.getUser().getId();
        var pageable = SupportTicketService.page(page);
        var movements = category == TicketCategory.CANJES
                ? transactions.findByUserPointsUserIdAndType(userId, TransactionType.REDEEM, pageable)
                : transactions.findByUserPointsUserId(userId, pageable);
        return new RewardSummary(points.availableBalance(userId), PagedResponse.from(movements.map(tx ->
                new Movement(tx.getId(), tx.getType(), tx.getPointsQuantity(), tx.getBalanceAfter(),
                        tx.getCreatedAt(), tx.getEvent() == null ? null : tx.getEvent().getTitle()))));
    }

    public record RewardSummary(long balance, PagedResponse<Movement> movements) {}
    public record Movement(Long id, TransactionType type, Integer points, Integer balanceAfter,
                           LocalDateTime createdAt, String eventTitle) {}
}
