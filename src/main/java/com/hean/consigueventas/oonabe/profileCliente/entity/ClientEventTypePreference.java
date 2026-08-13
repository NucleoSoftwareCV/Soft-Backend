package com.hean.consigueventas.oonabe.profileCliente.entity;

import com.hean.consigueventas.oonabe.experienceType.entity.ExperienceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "client_event_type_preferences",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_client_event_type_preference",
                columnNames = {"client_profile_id", "experience_type_id"}
        )
)
@Getter
@Setter
public class ClientEventTypePreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_profile_id", nullable = false)
    private ClientProfile clientProfile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "experience_type_id", nullable = false)
    private ExperienceType experienceType;
}
