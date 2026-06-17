package com.hean.consigueventas.oonabe.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Categoria visible en el catalogo.")
public record CategoryDTO(Long id, String name, String description, boolean active) {
}
