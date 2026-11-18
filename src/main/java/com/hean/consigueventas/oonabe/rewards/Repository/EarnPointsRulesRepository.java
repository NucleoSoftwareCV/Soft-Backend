package com.hean.consigueventas.oonabe.rewards.Repository;

import com.hean.consigueventas.oonabe.rewards.Entity.EarnPointsRules;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface    EarnPointsRulesRepository extends JpaRepository<EarnPointsRules, Long> {

    Optional<EarnPointsRules> findByExperienceType_Id(Long experienceTypeId);
}
