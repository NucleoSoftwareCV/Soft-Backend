package com.hean.consigueventas.oonabe.rewards.Repository;

import com.hean.consigueventas.oonabe.rewards.Entity.RedeemPointsRules;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RedeemPointsRulesRepository extends JpaRepository<RedeemPointsRules, Long> {
    Optional<RedeemPointsRules> findByExperienceType_Id(Long experienceTypeId);
}
