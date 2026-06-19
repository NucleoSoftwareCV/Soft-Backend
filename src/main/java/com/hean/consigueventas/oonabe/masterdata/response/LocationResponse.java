package com.hean.consigueventas.oonabe.masterdata.response;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ubicacion física activa.")
public record LocationResponse(Long id, String name, String address, String reference, Boolean isActive) {
}
