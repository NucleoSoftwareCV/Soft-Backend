package com.hean.consigueventas.oonabe.masterdata.entity;

import com.hean.consigueventas.oonabe.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "temas_trabajo")
@Getter
@Setter
public class WorkTopic extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String name;

    @Column(name = "activo", nullable = false)
    private boolean active = true;
}
//WorkTopic guardara los "temas que trabaja" cada profesional