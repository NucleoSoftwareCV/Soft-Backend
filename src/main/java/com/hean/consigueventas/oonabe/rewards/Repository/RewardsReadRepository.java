package com.hean.consigueventas.oonabe.rewards.Repository;

import com.hean.consigueventas.oonabe.rewards.Dto.Response.PointTransactionItem;
import com.hean.consigueventas.oonabe.rewards.Entity.UserPoints;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface RewardsReadRepository extends Repository<UserPoints, Long> {
    @Query("select p.availablePoints from UserPoints p where p.user.id = :userId")
    Optional<Integer> findBalance(@Param("userId") Long userId);

    @Query("""
            select new com.hean.consigueventas.oonabe.rewards.Dto.Response.PointTransactionItem(
                t.id, t.type, t.pointsQuantity, t.balanceAfter, t.createdAt)
            from PointsTransactions t where t.userPoints.user.id = :userId
            order by t.createdAt desc, t.id desc
            """)
    List<PointTransactionItem> findHistory(@Param("userId") Long userId, Pageable page);
}
