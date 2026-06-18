package com.hean.consigueventas.oonabe.catalog.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "cities")
@Getter
@Setter
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "El nombre de la ciudad es obligatorio")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotBlank(message = "El nombre de la provincia es obligatorio")
    @Column(name = "province", length = 100)
    private String province;

    @Size(max = 2, message = "El código de país debe tener máximo 2 caracteres")
    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode = "ES";

    @Column(name = "active", nullable = false)
    private Boolean isActive = true;
}
