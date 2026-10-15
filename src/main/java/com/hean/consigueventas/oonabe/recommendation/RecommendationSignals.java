package com.hean.consigueventas.oonabe.recommendation;

import com.hean.consigueventas.oonabe.common.enums.EventModality;

import java.util.Set;

public record RecommendationSignals(
        Set<Long> interestCategoryIds,
        Set<Long> favoriteCategoryIds,
        Set<Long> preferredExperienceTypeIds,
        Set<Long> followedSpecialistIds,
        EventModality preferredModality,
        String cityName
) {

    public RecommendationSignals {
        interestCategoryIds = copyOrEmpty(interestCategoryIds);
        favoriteCategoryIds = copyOrEmpty(favoriteCategoryIds);
        preferredExperienceTypeIds = copyOrEmpty(preferredExperienceTypeIds);
        followedSpecialistIds = copyOrEmpty(followedSpecialistIds);
    }

    private static Set<Long> copyOrEmpty(Set<Long> values) {
        return values == null ? Set.of() : Set.copyOf(values);
    }
}