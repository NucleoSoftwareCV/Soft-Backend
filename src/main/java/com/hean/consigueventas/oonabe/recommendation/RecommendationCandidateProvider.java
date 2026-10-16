package com.hean.consigueventas.oonabe.recommendation;

import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.repository.EventRepository;
import com.hean.consigueventas.oonabe.event.specification.EventSpecification;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class RecommendationCandidateProvider {

    private static final int MAX_CANDIDATES = 100;

    private final EventRepository eventRepository;
    private final Clock clock;

    public RecommendationCandidateProvider(EventRepository eventRepository, Clock clock) {
        this.eventRepository = eventRepository;
        this.clock = clock;
    }

    public List<Event> findCandidates(String cityName) {
        Instant now = clock.instant();
        String city = cityName == null ? null : cityName.strip();
        Specification<Event> futureOccurrence = EventSpecification.hasProgrammedOccurrenceBetween(now, null);
        Specification<Event> eligibility = futureOccurrence;

        if (city != null && !city.isBlank()) {
            Specification<Event> online = EventSpecification.hasModality(EventModality.ONLINE)
                    .and(futureOccurrence);
            Specification<Event> inCity = EventSpecification.hasModality(EventModality.PRESENCIAL)
                    .or(EventSpecification.hasModality(EventModality.HIBRIDA))
                    .and(EventSpecification.hasProgrammedOccurrenceInCityBetween(city, now, null));
            eligibility = online.or(inCity);
        }

        return eventRepository.findAll(
                EventSpecification.isPublished().and(eligibility),
                PageRequest.of(0, MAX_CANDIDATES, Sort.by("id"))
        ).getContent();
    }
}
