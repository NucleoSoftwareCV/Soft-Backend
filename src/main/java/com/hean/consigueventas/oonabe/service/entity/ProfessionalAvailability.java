package com.hean.consigueventas.oonabe.service.entity;

import com.hean.consigueventas.oonabe.location.entity.Location;
import com.hean.consigueventas.oonabe.profile.entity.SpecialistProfile;
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

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "disponibilidad_profesional")
@Getter
@Setter
public class ProfessionalAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesional_id", nullable = false)
    private SpecialistProfile specialist;

    @Column(name = "dia_semana", nullable = false)
    private Short dayOfWeek;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime startTime;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_id")
    private Location location;

    @Column(name = "vigente_desde", nullable = false)
    private LocalDate validFrom;

    @Column(name = "vigente_hasta")
    private LocalDate validUntil;

    @Column(name = "activo", nullable = false)
    private boolean active = true;
}
