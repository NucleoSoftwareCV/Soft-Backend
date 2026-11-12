package com.hean.consigueventas.oonabe.recommendation;

import com.hean.consigueventas.oonabe.campaign.dto.CampaignBadgeResponse;
import com.hean.consigueventas.oonabe.campaign.service.CampaignBadgeLookup;
import com.hean.consigueventas.oonabe.event.dto.request.EventFilterRequest;
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

import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventRecommendationQueryService {

    private final EventRecommendationService recommendationService;
    private final EventImageRepository imageRepository;
    private final CampaignBadgeLookup campaignBadgeLookup;
    private final Clock clock;

    public Page<RecommendedEventResponse> getRecommendations(
            String username,
            Pageable pageable
    ) {
        return buildResponsePage(
                recommendationService.getRecommendations(username, pageable)
        );
    }

    public Page<RecommendedEventResponse> getRecommendations(
            String username,
            Pageable pageable,
            EventFilterRequest filter,
            boolean useProfileCity
    ) {
        return buildResponsePage(
                recommendationService.getRecommendations(
                        username,
                        pageable,
                        filter,
                        useProfileCity
                )
        );
    }

    private Page<RecommendedEventResponse> buildResponsePage(
            Page<RecommendedEvent> recommendations
    ) {
        if (recommendations.isEmpty()) {
            return recommendations.map(result -> toResponse(result, null, List.of()));
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

        Map<Long, List<CampaignBadgeResponse>> badges =
                campaignBadgeLookup.openBadges(eventIds, clock.instant());

        return recommendations.map(result -> toResponse(
                result,
                coverUrls.get(result.event().getId()),
                badges.getOrDefault(result.event().getId(), List.of())
        ));
    }

    private RecommendedEventResponse toResponse(
            RecommendedEvent recommendation,
            String coverImageUrl,
            List<CampaignBadgeResponse> campaignBadges
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
                event.isRecurring(),
                event.getMood(),
                recommendation.occurrenceId(),
                campaignBadges
        );

        return new RecommendedEventResponse(
                card,
                recommendation.occurrenceId(),
                recommendation.score(),
                recommendation.reason()
        );
    }
}