package com.hean.consigueventas.oonabe.rewards.Entity;

import com.hean.consigueventas.oonabe.experienceType.entity.ExperienceType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "redeem_points_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedeemPointsRules {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "redeem_promotion", nullable = false)
    private BigDecimal redeemPromotion;

    @Column(name = "description", length = 255)
    private String description;

    @Column(nullable = false)
    private Boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experienceType_id", nullable = false)
    private ExperienceType experienceType;
}
