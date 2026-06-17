package com.hean.consigueventas.oonabe.event.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
public class EventCategoryLinkId implements Serializable {
    @Column(name = "evento_id")
    private Long eventId;

    @Column(name = "categoria_id")
    private Long categoryId;
}
