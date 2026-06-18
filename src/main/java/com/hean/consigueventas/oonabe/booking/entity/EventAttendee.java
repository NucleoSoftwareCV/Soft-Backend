package com.hean.consigueventas.oonabe.booking.entity;

import com.hean.consigueventas.oonabe.common.enums.AttendanceStatus;
import com.hean.consigueventas.oonabe.user.entity.User;
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

import java.time.Instant;

@Entity
@Table(name = "event_attendees")
@Getter
@Setter
public class EventAttendee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_booking_id", nullable = false)
    private EventBooking eventBooking;

    @Column(name = "attendee_name", length = 150)
    private String attendeeName;

    @Column(name = "attendee_email", length = 150)
    private String attendeeEmail;

    @Column(name = "attendee_phone", length = 25)
    private String attendeePhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status", nullable = false, length = 20)
    private AttendanceStatus attendanceStatus = AttendanceStatus.PENDIENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registered_by")
    private User registeredBy;

    @Column(name = "registered_at")
    private Instant registeredAt;
}
