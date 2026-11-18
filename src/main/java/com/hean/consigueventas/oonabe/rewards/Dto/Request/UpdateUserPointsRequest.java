package com.hean.consigueventas.oonabe.rewards.Dto.Request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdateUserPointsRequest {
    @NotNull(message = "UserId is required")
    private Long userId;

    @NotNull(message = "redeemPoints is required")
    private Integer redeemPoints;

}
