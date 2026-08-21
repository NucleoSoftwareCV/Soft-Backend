package com.hean.consigueventas.oonabe.event.dto.response;

import com.hean.consigueventas.oonabe.common.enums.EventStatus;

import java.util.List;

public record EventManagementResponse(
        EventDetailResponse event,
        EventStatus status,
        Long specialistId,
        List<EventOccurrenceResponse> occurrences
) {
}
