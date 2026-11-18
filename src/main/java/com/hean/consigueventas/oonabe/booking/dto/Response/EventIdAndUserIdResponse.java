package com.hean.consigueventas.oonabe.booking.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EventIdAndUserIdResponse {
    private Long attendeeId;
    private Long eventId;
    private Long userId;
}
