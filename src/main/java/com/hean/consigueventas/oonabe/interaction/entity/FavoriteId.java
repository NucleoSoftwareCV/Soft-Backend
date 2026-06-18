package com.hean.consigueventas.oonabe.interaction.entity;

import com.hean.consigueventas.oonabe.common.enums.FavoriteEntityType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
public class FavoriteId implements Serializable {
    @Column(name = "customer_id")
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", length = 15)
    private FavoriteEntityType entityType;

    @Column(name = "entity_id")
    private Long entityId;
}
