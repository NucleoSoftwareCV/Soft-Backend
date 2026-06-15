package com.hean.consigueventas.oonabe.location.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Ubicaciones")
@Getter
@Setter
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String reference;

    @Column(name = "isActive")
    private Boolean isActive =  false;

    @Column(nullable = false)
    private String locationLink;


}
