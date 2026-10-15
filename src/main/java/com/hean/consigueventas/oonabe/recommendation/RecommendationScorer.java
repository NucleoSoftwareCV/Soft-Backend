package com.hean.consigueventas.oonabe.recommendation;

import com.hean.consigueventas.oonabe.event.entity.Event;

import java.util.Objects;
import java.util.Set;

public final class RecommendationScorer {

    public RecommendationScore score(
            Event event,
            RecommendationSignals signals
    ) {
        Objects.requireNonNull(event, "El evento es obligatorio");
        Objects.requireNonNull(signals, "Las señales son obligatorias");

        Long specialistId = event.getSpecialist() == null
                ? null
                : event.getSpecialist().getId();

        Long categoryId = event.getCategory() == null
                ? null
                : event.getCategory().getId();

        Long experienceTypeId = event.getExperienceType() == null
                ? null
                : event.getExperienceType().getId();

        boolean followedSpecialist = matches(
                signals.followedSpecialistIds(),
                specialistId
        );

        boolean interestCategory = matches(
                signals.interestCategoryIds(),
                categoryId
        );

        boolean favoriteCategory = matches(
                signals.favoriteCategoryIds(),
                categoryId
        );

        boolean preferredExperienceType = matches(
                signals.preferredExperienceTypeIds(),
                experienceTypeId
        );

        boolean preferredModality =
                signals.preferredModality() != null
                        && signals.preferredModality() == event.getModality();

        int total = 0;

        if (followedSpecialist) {
            total += 40;
        }
        if (interestCategory) {
            total += 30;
        }
        if (favoriteCategory) {
            total += 15;
        }
        if (preferredExperienceType) {
            total += 10;
        }
        if (preferredModality) {
            total += 5;
        }

        String reason;

        if (followedSpecialist) {
            reason = "De un profesional que sigues";
        } else if (interestCategory) {
            reason = "Relacionado con tus intereses";
        } else if (favoriteCategory) {
            reason = "Relacionado con tus favoritos";
        } else if (preferredExperienceType || preferredModality) {
            reason = "Según tus preferencias";
        } else {
            reason = "Sin coincidencias";
        }

        return new RecommendationScore(total, reason);
    }

    private boolean matches(Set<Long> ids, Long id) {
        return id != null && ids.contains(id);
    }
}