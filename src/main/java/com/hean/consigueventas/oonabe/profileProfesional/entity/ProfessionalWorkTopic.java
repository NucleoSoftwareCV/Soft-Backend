package com.hean.consigueventas.oonabe.profileProfesional.entity;

import com.hean.consigueventas.oonabe.masterdata.entity.WorkTopic;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "professional_work_topics",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_professional_work_topic",
                        columnNames = {"specialist_id", "work_topic_id"}
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
    @JoinColumn(name = "specialist_id", nullable = false)
    private SpecialistProfile specialistProfile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_topic_id", nullable = false)
    private WorkTopic workTopic;
}
