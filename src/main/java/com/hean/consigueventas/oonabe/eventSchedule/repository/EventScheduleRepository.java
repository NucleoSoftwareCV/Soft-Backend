package com.hean.consigueventas.oonabe.eventSchedule.repository;

import com.hean.consigueventas.oonabe.eventSchedule.entity.EventSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface EventScheduleRepository extends JpaRepository<EventSchedule, Long> {

    //Buscar fecha por
    List<EventSchedule> findByDateBetweenOrderByDateAscStartTimeAsc(LocalDate startDate, LocalDate endDate);

    //Buscar fecha por
    List<EventSchedule> findByDateBetweenAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByDateAscStartTimeAsc(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime);


}
