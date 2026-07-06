package com.hean.consigueventas.oonabe.managementAdmin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RolePromotionRequestDto {
    @NotBlank(message = "El motivo (reason) es requerido")
    private String reason;
}
