package com.hean.consigueventas.oonabe.oneToOneSession.entity;

import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "bloqueos_agenda")
@Getter
@Setter
public class ScheduleBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesional_id", nullable = false)
    private SpecialistProfile specialist;

    @Column(name = "inicio_at", nullable = false)
    private Instant startsAt;

    @Column(name = "fin_at", nullable = false)
    private Instant endsAt;

    @Column(name = "motivo")
    private String reason;
}
