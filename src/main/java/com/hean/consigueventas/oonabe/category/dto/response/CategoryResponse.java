package com.hean.consigueventas.oonabe.category.dto.response;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Categoría visible en el catálogo.")
public record CategoryResponse(Long id, String name, String description, boolean active) {
}
