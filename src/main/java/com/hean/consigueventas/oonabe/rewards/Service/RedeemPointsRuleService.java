package com.hean.consigueventas.oonabe.rewards.Service;

import com.hean.consigueventas.oonabe.rewards.Entity.RedeemPointsRules;
import com.hean.consigueventas.oonabe.rewards.Repository.RedeemPointsRulesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RedeemPointsRuleService {

    private final RedeemPointsRulesRepository redeemPointsRulesRepository;

    public BigDecimal calculateRedemptionAmount(Long experienceTypeId, Integer redeemPoints) {
        RedeemPointsRules redeemPointsRules = redeemPointsRulesRepository
                .findByExperienceType_Id(experienceTypeId)
                .orElseThrow(() -> new RuntimeException("EarnPointRule not found"));

        BigDecimal promotion = redeemPointsRules.getRedeemPromotion();
        BigDecimal redeemPointsValue = new BigDecimal(redeemPoints);
        return redeemPointsValue.multiply(promotion);
    }
}
