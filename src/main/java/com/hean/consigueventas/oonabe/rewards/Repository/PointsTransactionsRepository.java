package com.hean.consigueventas.oonabe.rewards.Repository;

import com.hean.consigueventas.oonabe.rewards.Entity.PointsTransactions;
import com.hean.consigueventas.oonabe.rewards.Enum.TransactionType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PointsTransactionsRepository extends JpaRepository<PointsTransactions, Long> {

    @EntityGraph(attributePaths = "event")
    Page<PointsTransactions> findByUserPointsUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = "event")
    Page<PointsTransactions> findByUserPointsUserIdAndType(Long userId, TransactionType type, Pageable pageable);

    @EntityGraph(attributePaths = {
            "userPoints",
            "userPoints.user"
    })
    List<PointsTransactions> findByUserPointsUserEmail(String email);

    List<PointsTransactions> findByUserPointsUserEmailAndType(String email, TransactionType type);
}
