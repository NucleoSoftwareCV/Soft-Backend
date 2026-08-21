package com.hean.consigueventas.oonabe.masterdata.entity;

import com.hean.consigueventas.oonabe.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "work_topics")
@Getter
@Setter
public class WorkTopic extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
