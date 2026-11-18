package com.hean.consigueventas.oonabe.rewards.Entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rewards_legal_bases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardLegalBases {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 80)
    private String question;
    @Column(nullable = false, columnDefinition = "TEXT", length = 200)
    private String description;
}
