package com.hean.consigueventas.oonabe.rewards.Service;

import com.hean.consigueventas.oonabe.rewards.Entity.EarnPointsRules;
import com.hean.consigueventas.oonabe.rewards.Repository.EarnPointsRulesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EarnPointsRuleService {

    private final EarnPointsRulesRepository earnPointsRulesRepository;

    /** Returns rule, must exist n be active (flow earnPoints). */
    public EarnPointsRules getActiveRuleByExperienceTypeId(Long experienceTypeId) {
        EarnPointsRules rule = earnPointsRulesRepository.findByExperienceType_Id(experienceTypeId)
                .orElseThrow(() -> new RuntimeException("EarnPointRule not found"));

        if (!rule.getActive()) {
            throw new IllegalStateException("Point rule is not active");
        }
        return rule;
    }

    public EarnPointsRules findRuleById(Long ruleId) {
        return earnPointsRulesRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("EarnPointRule not found"));
    }
}
