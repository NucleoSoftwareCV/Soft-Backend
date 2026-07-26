package com.hean.consigueventas.oonabe.interaction.dto;

import java.util.Set;

public record FavoriteIdsResponse(
    Set<Long> eventIds,
    Set<Long> serviceIds,
    Set<Long> professionalIds
) {}