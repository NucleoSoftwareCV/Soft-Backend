package com.hean.consigueventas.oonabe.recommendation;

import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import com.hean.consigueventas.oonabe.event.repository.EventOccurrenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventRecommendationService {

    private static final int MAX_PAGE_SIZE = 100;

    private static final Comparator<EventOccurrence> OCCURRENCE_ORDER =
            Comparator.comparing(EventOccurrence::getStartsAt)
                    .thenComparing(EventOccurrence::getId);

    private static final Comparator<RecommendedEvent> RECOMMENDATION_ORDER =
            Comparator.comparingInt(RecommendedEvent::score)
                    .reversed()
                    .thenComparing(RecommendedEvent::startsAt)
                    .thenComparing(result -> result.event().getId());

    private final RecommendationSignalsService signalsService;
    private final EventOccurrenceRepository occurrenceRepository;
    private final Clock clock;

    private final RecommendationScorer scorer = new RecommendationScorer();

    public Page<RecommendedEvent> getRecommendations(
            String username,
            Pageable pageable
    ) {
        Objects.requireNonNull(username, "El usuario es obligatorio");
        validatePageable(pageable);

        RecommendationSignals signals = signalsService.getSignals(username);

        String cityName = normalizeCity(signals.cityName());

        List<EventOccurrence> candidates =
                occurrenceRepository.findRecommendationCandidates(
                        clock.instant(),
                        cityName
                );

        Map<Long, EventOccurrence> nearestByEvent = new HashMap<>();

        for (EventOccurrence occurrence : candidates) {
            nearestByEvent.merge(
                    occurrence.getEvent().getId(),
                    occurrence,
                    (current, incoming) ->
                            OCCURRENCE_ORDER.compare(current, incoming) <= 0
                                    ? current
                                    : incoming
            );
        }

        List<RecommendedEvent> ordered = nearestByEvent.values()
                .stream()
                .map(occurrence -> toRecommendation(occurrence, signals))
                .sorted(RECOMMENDATION_ORDER)
                .toList();

        int from = (int) Math.min(pageable.getOffset(), ordered.size());

        int to = (int) Math.min(
                (long) from + pageable.getPageSize(),
                ordered.size()
        );

        return new PageImpl<>(
                ordered.subList(from, to),
                pageable,
                ordered.size()
        );
    }

    private RecommendedEvent toRecommendation(
            EventOccurrence occurrence,
            RecommendationSignals signals
    ) {
        RecommendationScore result = scorer.score(
                occurrence.getEvent(),
                signals
        );

        String cityName = occurrence.getEvent().getModality()
                == EventModality.ONLINE
                ? null
                : occurrence.getLocation().getCity().getName();

        return new RecommendedEvent(
                occurrence.getEvent(),
                occurrence.getId(),
                occurrence.getStartsAt(),
                cityName,
                result.score(),
                result.reason()
        );
    }

    private void validatePageable(Pageable pageable) {
        Objects.requireNonNull(pageable, "La paginación es obligatoria");

        if (pageable.isUnpaged()) {
            throw new IllegalArgumentException(
                    "La consulta requiere paginación"
            );
        }

        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    "El tamaño máximo de página es " + MAX_PAGE_SIZE
            );
        }

        if (pageable.getSort().isSorted()) {
            throw new IllegalArgumentException(
                    "Este servicio utiliza el orden de recomendaciones"
            );
        }
    }

    private String normalizeCity(String cityName) {
        if (cityName == null || cityName.isBlank()) {
            return null;
        }

        return cityName.strip();
    }
}