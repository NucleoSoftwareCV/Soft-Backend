package com.hean.consigueventas.oonabe.managementAdmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RolePromotionRequestDto {
    @NotBlank(message = "El motivo (reason) es requerido")
    @Size(max = 500, message = "El motivo no puede superar los 500 caracteres")
    private String reason;
}
