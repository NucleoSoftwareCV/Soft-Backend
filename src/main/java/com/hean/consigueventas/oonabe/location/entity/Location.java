package com.hean.consigueventas.oonabe.location.entity;


import com.hean.consigueventas.oonabe.catalog.entity.City;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ubicaciones")
@Getter
@Setter
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String name;

    @Column(name = "direccion", nullable = false)
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ciudad_id")
    private City city;

    @Column(name = "latitud", precision = 9, scale = 6)
    private java.math.BigDecimal latitude;

    @Column(name = "longitud", precision = 9, scale = 6)
    private java.math.BigDecimal longitude;

    @Column(name = "indicaciones", columnDefinition = "TEXT")
    private String reference;

    @Column(name = "activo", nullable = false)
    private Boolean isActive =  false;

}
