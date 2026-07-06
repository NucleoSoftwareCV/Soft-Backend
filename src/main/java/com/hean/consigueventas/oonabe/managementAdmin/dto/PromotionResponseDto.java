package com.hean.consigueventas.oonabe.managementAdmin.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class PromotionResponseDto {
    private Long id;
    private Long userId;
    private String status;
    private String reason;
    private Instant createdAt;
}
