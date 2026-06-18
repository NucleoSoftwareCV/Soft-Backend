package com.hean.consigueventas.oonabe.payment.entity;

import com.hean.consigueventas.oonabe.common.security.SensitiveStringCryptoConverter;
import com.hean.consigueventas.oonabe.profile.entity.CustomerProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "customer_payment_methods")
@Getter
@Setter
public class CustomerPaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerProfile customer;

    @Column(name = "provider", nullable = false, length = 50)
    private String provider;

    @Convert(converter = SensitiveStringCryptoConverter.class)
    @Column(name = "encrypted_token", nullable = false, columnDefinition = "TEXT")
    private String token;

    @Column(name = "brand", length = 40)
    private String brand;

    @Column(name = "last4", length = 4)
    private String last4;

    @Column(name = "exp_month")
    private Short expirationMonth;

    @Column(name = "exp_year")
    private Short expirationYear;

    @Column(name = "default_method", nullable = false)
    private boolean defaultMethod;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
