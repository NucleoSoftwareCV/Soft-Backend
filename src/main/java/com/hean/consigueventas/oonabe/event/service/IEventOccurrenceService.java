package com.hean.consigueventas.oonabe.event.service;

import com.hean.consigueventas.oonabe.event.dto.request.EventOccurrenceUpsertRequest;
import com.hean.consigueventas.oonabe.event.dto.response.EventOccurrenceAdminResponse;
import com.hean.consigueventas.oonabe.event.dto.response.EventOccurrencePublicResponse;
import java.time.LocalDate;
import java.util.List;

public interface IEventOccurrenceService {
    List<EventOccurrenceAdminResponse> getAllOccurrences();
    List<EventOccurrencePublicResponse> getPublicOccurrences();
    List<EventOccurrencePublicResponse> getPublicOccurrencesByDate(LocalDate date);
    List<EventOccurrencePublicResponse> getPublicOccurrencesByDateRange(LocalDate startDate, LocalDate endDate);
    List<EventOccurrenceAdminResponse> getOccurrencesByEvent(Long eventId);
    EventOccurrenceAdminResponse getOccurrenceById(Long id);
    List<EventOccurrenceAdminResponse> filterOccurrences(String dateFilter, String timeFilter, LocalDate selectedDate);
    EventOccurrenceAdminResponse createOccurrence(EventOccurrenceUpsertRequest request);
    EventOccurrenceAdminResponse updateOccurrence(Long id, EventOccurrenceUpsertRequest request);
    void deleteOccurrence(Long id);
}
