package com.hean.consigueventas.oonabe.interaction.dto;

import com.hean.consigueventas.oonabe.common.enums.FavoriteEntityType;
import jakarta.validation.constraints.NotNull;

public record FavoriteToggleRequest(
    @NotNull FavoriteEntityType entityType,
    @NotNull Long entityId
) {}