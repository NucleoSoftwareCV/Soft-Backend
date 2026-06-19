package com.hean.consigueventas.oonabe.profileProfesional.entity;

import com.hean.consigueventas.oonabe.masterdata.entity.WorkTopic;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        //En temas_profesional no pueden existir dos filas con el mismo profesional_id y el mismo tema_id
        name = "temas_profesional",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_profesional_tema",
                        columnNames = {"profesional_id", "tema_id"}
                )
        }
)
@Getter
@Setter
public class ProfessionalWorkTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profesional_id", nullable = false)
    private SpecialistProfile specialistProfile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tema_id", nullable = false)
    private WorkTopic workTopic;
}