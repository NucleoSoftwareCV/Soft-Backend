package com.hean.consigueventas.oonabe.profile.entity;

import com.hean.consigueventas.oonabe.catalog.entity.City;
import com.hean.consigueventas.oonabe.common.entity.AuditableEntity;
import com.hean.consigueventas.oonabe.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "customer_profiles")
@Getter
@Setter
public class CustomerProfile extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "first_names", nullable = false, length = 100)
    private String firstNames;

    @Column(name = "last_names", nullable = false, length = 100)
    private String lastNames;

    @Column(name = "phone", length = 25)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_city_id")
    private City preferredCity;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;
}
