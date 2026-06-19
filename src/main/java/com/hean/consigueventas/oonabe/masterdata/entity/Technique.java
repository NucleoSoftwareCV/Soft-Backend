package com.hean.consigueventas.oonabe.masterdata.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tecnicas")
@Getter
@Setter
public class Technique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true, length = 150)
    private String name;

    @Column(name = "activo", nullable = false)
    private boolean active = true;
}
//Technique guardara las "técnicas que ofrezcan" cada profesional