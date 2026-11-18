package com.hean.consigueventas.oonabe.rewards.Dto.Request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RedeemPointsRequest {
    @NotNull(message = "The userId is required")
    private Long userId;

    @NotNull(message = "The eventId is required")
    private Long eventId;

    @NotNull(message = "The redeemPoints is required")
    private Integer redeemPoints;
}
