package com.hean.consigueventas.oonabe.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", unique = true, nullable = false, length = 40)
    private String name;

    @Column(name = "nombre", nullable = false, length = 80)
    private String displayName;

    @Column(name = "descripcion")
    @Size(max = 255)
    private String description;

    @Builder.Default
    @Column(name = "activo", nullable = false)
    private boolean active = true;

    public String getCodigo() {
        return name;
    }

    public void setCodigo(String codigo) {
        this.name = codigo;
    }

    public String getNombre() {
        return displayName;
    }

    public void setNombre(String nombre) {
        this.displayName = nombre;
    }

    @PrePersist
    @PreUpdate
    void fillDisplayName() {
        if (displayName == null || displayName.isBlank()) {
            displayName = description != null && !description.isBlank() ? description : name;
        }
    }
}
