package com.hean.consigueventas.oonabe.profileCliente.entity;
import jakarta.persistence.*;
import lombok.*;
@Embeddable @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ReferenceLocation {
    @Column(name="reference_label", length=300) private String label;
    @Column(name="reference_latitude") private Double latitude;
    @Column(name="reference_longitude") private Double longitude;
    @Column(name="reference_approximate") private Boolean approximate;
}
