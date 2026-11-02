package com.hean.consigueventas.oonabe.campaign.service;

import com.hean.consigueventas.oonabe.campaign.domain.CampaignStatus;
import com.hean.consigueventas.oonabe.campaign.dto.CampaignBadgeResponse;
import com.hean.consigueventas.oonabe.campaign.repository.PromoCampaignEventRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Resuelve en lote los distintivos de campañas abiertas para las tarjetas y el detalle de eventos. */
@Component
public class CampaignBadgeLookup {

    private final PromoCampaignEventRepository campaignEvents;

    public CampaignBadgeLookup(PromoCampaignEventRepository campaignEvents) {
        this.campaignEvents = campaignEvents;
    }

    public Map<Long, List<CampaignBadgeResponse>> openBadges(Collection<Long> eventIds, Instant now) {
        if (eventIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, List<CampaignBadgeResponse>> badges = new HashMap<>();
        for (PromoCampaignEventRepository.BadgeRow row : campaignEvents.findOpenBadges(eventIds, CampaignStatus.PUBLICADA, now)) {
            badges.computeIfAbsent(row.getEventId(), id -> new ArrayList<>())
                    .add(new CampaignBadgeResponse(row.getSlug(), row.getBadgeLabel()));
        }
        return badges;
    }

    public List<CampaignBadgeResponse> openBadges(Long eventId, Instant now) {
        return openBadges(List.of(eventId), now).getOrDefault(eventId, List.of());
    }
}
