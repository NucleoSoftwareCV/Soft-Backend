package com.hean.consigueventas.oonabe.recommendation;

import com.hean.consigueventas.oonabe.event.entity.Event;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class RecommendationScoringService {

    private final RecommendationScorer scorer = new RecommendationScorer();

    public List<ScoredEvent> scoreAndSort(List<Event> candidates, RecommendationSignals signals) {
        Objects.requireNonNull(candidates, "Los candidatos son obligatorios");
        Objects.requireNonNull(signals, "Las señales son obligatorias");
        candidates.forEach(event -> Objects.requireNonNull(event, "El evento es obligatorio"));

        return candidates.stream()
                .map(event -> new ScoredEvent(event, scorer.score(event, signals)))
                .sorted(Comparator.comparingInt((ScoredEvent result) -> result.score().score()).reversed())
                .toList();
    }

    public record ScoredEvent(Event event, RecommendationScore score) {
    }
}
