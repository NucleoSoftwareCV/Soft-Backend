package com.hean.consigueventas.oonabe.profileCliente.entity;

import com.hean.consigueventas.oonabe.common.enums.EventModality;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "client_modality_preferences")
@Getter
@Setter
public class ClientModalityPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_profile_id", nullable = false, unique = true)
    private ClientProfile clientProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "modality", nullable = false, length = 20)
    private EventModality modality;
}
