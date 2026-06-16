package com.hean.consigueventas.oonabe.catalog.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "El nombre de la ciudad es obligatorio")
    @Column(name = "nombre", nullable = false, length = 100)
    private String name;

    @NotBlank(message = "El nombre de la provincia es obligatorio")
    @Column(name = "provincia", length = 100)
    private String province;

    @Size(max = 2, message = "El código de país debe tener máximo 2 caracteres")
    @Column(name = "pais_codigo", nullable = false, length = 2)
    private String countryCode = "ES";

    @Column(name = "is_active")
    private Boolean isActive = true;
}
