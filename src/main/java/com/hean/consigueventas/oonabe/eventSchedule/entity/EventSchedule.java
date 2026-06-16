package com.hean.consigueventas.oonabe.eventSchedule.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "horarios_Evento")
@Getter
@Setter
public class EventSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_horario_evento")
    private Long id;

    @NotNull                    //No permite valores nulos en java
    @Column(nullable = false)   //No permite valores nulos en en la bd
    private LocalDate date;

    @NotNull
    @Column(nullable = false)
    private LocalTime startTime;

    @NotNull
    @Column(nullable = false)
    private LocalTime endTime;

    @NotNull
    @Column(nullable = false)
    private boolean booked;

    @NotNull
    @Min(value = 0)
    @Max(value = 100)
    @Column(nullable = false)
    private Integer maxCapacity;

    @NotNull
    @Min(value = 1)
    @Max(value = 60)
    @Column(nullable = false)
    private Integer availableSpots;

//    @ManyToOne
//    @JoinColumn (name = "event_id", nullable = false)
//    private Event event;




}