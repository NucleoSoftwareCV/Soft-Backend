package com.hean.consigueventas.oonabe.recommendation;

import com.hean.consigueventas.oonabe.event.dto.response.EventCardResponse;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.entity.EventImage;
import com.hean.consigueventas.oonabe.event.repository.EventImageRepository;
import com.hean.consigueventas.oonabe.experienceType.entity.ExperienceType;
import com.hean.consigueventas.oonabe.recommendation.dto.response.RecommendedEventResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventRecommendationQueryService {

    private final EventRecommendationService recommendationService;
    private final EventImageRepository imageRepository;

    public Page<RecommendedEventResponse> getRecommendations(
            String username,
            Pageable pageable
    ) {
        Page<RecommendedEvent> recommendations =
                recommendationService.getRecommendations(username, pageable);

        if (recommendations.isEmpty()) {
            return recommendations.map(result -> toResponse(result, null));
        }

        List<Long> eventIds = recommendations.getContent()
                .stream()
                .map(result -> result.event().getId())
                .toList();

        Map<Long, String> coverUrls = new HashMap<>();

        for (EventImage image :
                imageRepository.findOrderedCandidatesByEventIds(eventIds)) {
            coverUrls.putIfAbsent(
                    image.getEvent().getId(),
                    image.getUrl()
            );
        }

        return recommendations.map(result -> toResponse(
                result,
                coverUrls.get(result.event().getId())
        ));
    }

    private RecommendedEventResponse toResponse(
            RecommendedEvent recommendation,
            String coverImageUrl
    ) {
        Event event = recommendation.event();
        ExperienceType experienceType = event.getExperienceType();

        EventCardResponse card = new EventCardResponse(
                event.getId(),
                event.getTitle(),
                event.getSummary(),
                event.getModality(),
                event.getPriceFrom(),
                event.getCurrency(),
                event.getCategory().getId(),
                event.getCategory().getName(),
                event.getSpecialist().getId(),
                event.getSpecialist().getPublicName(),
                event.getSpecialist().getPhotoUrl(),
                coverImageUrl,
                recommendation.startsAt(),
                recommendation.endsAt(),
                recommendation.cityName(),
                experienceType == null ? null : experienceType.getId(),
                experienceType == null ? null : experienceType.getName(),
                experienceType == null ? null : experienceType.getSlug(),
                event.isRecurring()
        );

        return new RecommendedEventResponse(
                card,
                recommendation.occurrenceId(),
                recommendation.score(),
                recommendation.reason()
        );
    }
}