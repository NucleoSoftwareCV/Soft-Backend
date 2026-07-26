package com.hean.consigueventas.oonabe.interaction.dto;

import com.hean.consigueventas.oonabe.common.enums.FavoriteEntityType;
import java.math.BigDecimal;
import java.time.Instant;

public record FavoriteResponse(
    FavoriteEntityType entityType,
    Long entityId,
    String title,
    String imageUrl,
    String categoryName,
    String locationName,
    BigDecimal price,
    String currency,
    String slug,
    String organizerName,
    Instant startsAt,
    String recurrenceLabel
) {}