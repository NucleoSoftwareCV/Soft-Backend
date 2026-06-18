package com.hean.consigueventas.oonabe.event.entity;

import com.hean.consigueventas.oonabe.common.enums.ImageFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "event_images")
@Getter
@Setter
public class EventImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "url", nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "alternative_text", length = 180)
    private String alternativeText;

    @Column(name = "cover", nullable = false)
    private boolean cover;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", length = 10)
    private ImageFormat format;

    @Column(name = "size_bytes")
    private Integer sizeBytes;

    @Column(name = "sort_order", nullable = false)
    private Short sortOrder = 0;
}
