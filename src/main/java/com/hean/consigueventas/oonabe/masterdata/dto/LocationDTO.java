package com.hean.consigueventas.oonabe.masterdata.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ubicacion fisica activa.")
public record LocationDTO(Long id, String name, String address, String reference, Boolean isActive) {
}
