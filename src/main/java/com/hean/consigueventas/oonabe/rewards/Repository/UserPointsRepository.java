package com.hean.consigueventas.oonabe.rewards.Repository;

import com.hean.consigueventas.oonabe.rewards.Entity.UserPoints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserPointsRepository extends JpaRepository<UserPoints, Long> {

    @Query("SELECT up.availablePoints " +
            "FROM UserPoints up " +
            "WHERE up.user.email = :email")
    Optional<Integer> findAvailablePointsByUserEmail(@Param("email") String email);

    @Query("select coalesce(sum(up.availablePoints), 0) from UserPoints up where up.user.id = :userId")
    long availableBalance(@Param("userId") Long userId);

    Optional<UserPoints> findByUserEmail(String email);
}
