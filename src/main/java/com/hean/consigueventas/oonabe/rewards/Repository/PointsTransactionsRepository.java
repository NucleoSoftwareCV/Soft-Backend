package com.hean.consigueventas.oonabe.rewards.Repository;

import com.hean.consigueventas.oonabe.rewards.Entity.PointsTransactions;
import com.hean.consigueventas.oonabe.rewards.Enum.TransactionType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointsTransactionsRepository extends JpaRepository<PointsTransactions, Long> {

    @EntityGraph(attributePaths = {
            "userPoints",
            "userPoints.user"
    })
    List<PointsTransactions> findByUserPointsUserEmail(String email);

    List<PointsTransactions> findByUserPointsUserEmailAndType(String email, TransactionType type);
}
