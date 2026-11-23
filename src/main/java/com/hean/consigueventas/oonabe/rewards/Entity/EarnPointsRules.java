package com.hean.consigueventas.oonabe.rewards.Entity;

import com.hean.consigueventas.oonabe.experienceType.entity.ExperienceType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "earn_points_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class EarnPointsRules {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reward", nullable = false)
    private Integer reward;

    @Column(name = "description", length = 255)
    private String description;

    @Column(nullable = false)
    private Boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experienceType_id", nullable = false)
    private ExperienceType experienceType;

}