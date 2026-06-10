package com.hean.consigueventas.oonabe.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // Ejemplo: "ADMIN"

    @Column(unique = true, nullable = false)
    @Size(max = 255)
    private String description;

    @Column(columnDefinition = "boolean default true")
    private boolean isActive = true;
}
