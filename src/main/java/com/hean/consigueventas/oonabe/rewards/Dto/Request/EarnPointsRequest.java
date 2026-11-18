package com.hean.consigueventas.oonabe.rewards.Dto.Request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class EarnPointsRequest {
    @NotNull(message = "El eventAttendanceId es obligatorio")
    private Long eventAttendanceId;

}
