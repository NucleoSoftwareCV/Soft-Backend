package com.hean.consigueventas.oonabe.common.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "Respuesta paginada estable para listados publicos")
public record PagedResponse<T>(
        @Schema(description = "Elementos de la pagina actual")
        List<T> content,

        @Schema(description = "Total de elementos disponibles")
        long totalElements,

        @Schema(description = "Total de paginas disponibles")
        int totalPages,

        @Schema(description = "Tamano solicitado de pagina")
        int size,

        @Schema(description = "Numero de pagina actual, empezando en 0")
        int number,

        @Schema(description = "Cantidad de elementos en la pagina actual")
        int numberOfElements,

        @Schema(description = "Indica si es la primera pagina")
        boolean first,

        @Schema(description = "Indica si es la ultima pagina")
        boolean last,

        @Schema(description = "Indica si la pagina no contiene elementos")
        boolean empty
) {
    public static <T> PagedResponse<T> from(Page<T> page) {
        return new PagedResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getSize(),
                page.getNumber(),
                page.getNumberOfElements(),
                page.isFirst(),
                page.isLast(),
                page.isEmpty()
        );
    }
}
