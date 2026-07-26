package com.hean.consigueventas.oonabe.interaction.mapper;

import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.interaction.dto.response.EventFavoriteResponse;
import com.hean.consigueventas.oonabe.interaction.dto.response.FollowedProfessionalResponse;
import com.hean.consigueventas.oonabe.interaction.entity.EventFavorite;
import com.hean.consigueventas.oonabe.interaction.entity.ProfessionalFollow;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import org.springframework.stereotype.Component;

@Component
public class InteractionMapper {

    public EventFavoriteResponse toEventFavoriteResponse(
            EventFavorite eventFavorite
    ) {
        Event event = eventFavorite.getEvent();

        return new EventFavoriteResponse(
                eventFavorite.getId(),
                event.getId(),
                event.getTitle(),
                event.getSummary(),
                event.getCategory().getName(),
                event.getModality(),
                event.getEventType(),
                event.getPriceFrom(),
                event.getCurrency(),
                eventFavorite.getCreatedAt()
        );
    }

    public FollowedProfessionalResponse toFollowedProfessionalResponse(
            ProfessionalFollow professionalFollow
    ) {
        SpecialistProfile specialistProfile =
                professionalFollow.getSpecialistProfile();

        return new FollowedProfessionalResponse(
                specialistProfile.getId(),
                specialistProfile.getSlug(),
                specialistProfile.getPublicName(),
                specialistProfile.getProfileCategory(),
                specialistProfile.getBiography(),
                specialistProfile.getPhotoUrl(),
                professionalFollow.getFollowedAt()
        );
    }
}