package com.hean.consigueventas.oonabe.eventSchedule.repository;

import com.hean.consigueventas.oonabe.eventSchedule.entity.EventSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface EventScheduleRepository extends JpaRepository<EventSchedule, Long> {

    //Consulta los horarios de una fecha exacta y los ordena por hora de inicio ascendente
    List<EventSchedule> findByDateOrderByStartTimeAsc(LocalDate date);

    //Consulta los horarios dentro de un rango de fechas y los ordena por fecha y hora de inicio
    List<EventSchedule> findByDateBetweenOrderByDateAscStartTimeAsc(LocalDate startDate, LocalDate endDate);

    //Consulta los horarios dentro de un rango de fechas y franja horaria
    List<EventSchedule> findByDateBetweenAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByDateAscStartTimeAsc(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime);


}
