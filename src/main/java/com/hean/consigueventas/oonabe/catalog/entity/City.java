package com.hean.consigueventas.oonabe.catalog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ciudades")
@Getter
@Setter
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String name;

    @Column(name = "provincia", length = 100)
    private String province;

    @Column(name = "pais_codigo", nullable = false, length = 2)
    private String countryCode = "ES";

    @Column(name = "activo", nullable = false)
    private boolean active = true;
}
