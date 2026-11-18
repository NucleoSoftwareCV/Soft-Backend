package com.hean.consigueventas.oonabe.rewards.Config;

import com.hean.consigueventas.oonabe.experienceType.entity.ExperienceType;
import com.hean.consigueventas.oonabe.experienceType.repository.ExperienceTypeRepository;
import com.hean.consigueventas.oonabe.rewards.Entity.EarnPointsRules;
import com.hean.consigueventas.oonabe.rewards.Entity.RedeemPointsRules;
import com.hean.consigueventas.oonabe.rewards.Repository.EarnPointsRulesRepository;
import com.hean.consigueventas.oonabe.rewards.Repository.RedeemPointsRulesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PointsRulesDataSeeder implements CommandLineRunner {

    private final EarnPointsRulesRepository earnPointsRulesRepository;
    private final RedeemPointsRulesRepository redeemPointsRulesRepository;
    private final ExperienceTypeRepository experienceTypeRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (earnPointsRulesRepository.count() > 0 &&
                redeemPointsRulesRepository.count() > 0) {
            return;
        }

        Map<Long, ExperienceType> experienceTypes =
                experienceTypeRepository.findAllById(List.of(1L, 2L, 3L, 4L, 5L, 6L))
                        .stream()
                        .collect(Collectors.toMap(
                                ExperienceType::getId,
                                Function.identity()
                        ));

        if (earnPointsRulesRepository.count() == 0) {
            List<EarnPointsRules> earnRules = List.of(
                    EarnPointsRules.builder()
                            .reward(10)
                            .description("puntos de retiro")
                            .active(true)
                            .experienceType(experienceTypes.get(1L))
                            .build(),
                    EarnPointsRules.builder()
                            .reward(20)
                            .description("puntos de talleres")
                            .active(true)
                            .experienceType(experienceTypes.get(2L))
                            .build(),
                    EarnPointsRules.builder()
                            .reward(25)
                            .description("puntos de ceremonias")
                            .active(true)
                            .experienceType(experienceTypes.get(3L))
                            .build(),
                    EarnPointsRules.builder()
                            .reward(30)
                            .description("puntos de encuentros grupales")
                            .active(true)
                            .experienceType(experienceTypes.get(4L))
                            .build(),
                    EarnPointsRules.builder()
                            .reward(35)
                            .description("puntos de formaciones")
                            .active(true)
                            .experienceType(experienceTypes.get(5L))
                            .build(),
                    EarnPointsRules.builder()
                            .reward(45)
                            .description("puntos de clases")
                            .active(true)
                            .experienceType(experienceTypes.get(6L))
                            .build()

            );
            earnPointsRulesRepository.saveAll(earnRules);
        }

        if (redeemPointsRulesRepository.count() == 0) {
            List<RedeemPointsRules> redeemRules = List.of(
                    RedeemPointsRules.builder()
                            .redeemPromotion(new BigDecimal("0.01"))
                            .description("puntos de retiro")
                            .active(true)
                            .experienceType(experienceTypes.get(1L))
                            .build(),
                    RedeemPointsRules.builder()
                            .redeemPromotion(new BigDecimal("0.01"))
                            .description("puntos de talleres")
                            .active(true)
                            .experienceType(experienceTypes.get(2L))
                            .build(),
                    RedeemPointsRules.builder()
                            .redeemPromotion(new BigDecimal("0.01"))
                            .description("puntos de ceremonias")
                            .active(true)
                            .experienceType(experienceTypes.get(3L))
                            .build(),
                    RedeemPointsRules.builder()
                            .redeemPromotion(new BigDecimal("0.01"))
                            .description("puntos de encuentros grupales")
                            .active(true)
                            .experienceType(experienceTypes.get(4L))
                            .build(),
                    RedeemPointsRules.builder()
                            .redeemPromotion(new BigDecimal("0.01"))
                            .description("puntos de formaciones")
                            .active(true)
                            .experienceType(experienceTypes.get(5L))
                            .build(),
                    RedeemPointsRules.builder()
                            .redeemPromotion(new BigDecimal("0.01"))
                            .description("puntos de clases")
                            .active(true)
                            .experienceType(experienceTypes.get(6L))
                            .build()
            );
            redeemPointsRulesRepository.saveAll(redeemRules);
        }
    }
}
