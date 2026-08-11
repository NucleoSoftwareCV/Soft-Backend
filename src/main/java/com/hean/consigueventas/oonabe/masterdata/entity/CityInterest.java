package com.hean.consigueventas.oonabe.masterdata.entity;

import com.hean.consigueventas.oonabe.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "city_interests", uniqueConstraints = {
        @UniqueConstraint(name = "uk_city_interest_city_email", columnNames = {"city_id", "email"})
})
@Getter
@Setter
public class CityInterest extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Column(name = "email", nullable = false, length = 150)
    private String email;
}
