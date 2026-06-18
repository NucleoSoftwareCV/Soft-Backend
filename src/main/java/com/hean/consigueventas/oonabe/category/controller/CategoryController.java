package com.hean.consigueventas.oonabe.category.controller;

import com.hean.consigueventas.oonabe.category.dto.request.CategoryUpsertRequest;
import com.hean.consigueventas.oonabe.category.dto.response.CategoryResponse;
import com.hean.consigueventas.oonabe.category.service.ICategoryService;
import com.hean.consigueventas.oonabe.common.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Categorías", description = "Catálogo de categorías de eventos.")
public class CategoryController {
    private final ICategoryService categoryService;

    public CategoryController(ICategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @Operation(summary = "Listar categorías activas", description = "Devuelve las categorías visibles para el catálogo público.", security = {})
    @ApiResponse(responseCode = "200", description = "Categorías activas")
    public List<CategoryResponse> findActive() {
        return categoryService.findActive();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear categoría", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public CategoryResponse create(@Valid @RequestBody CategoryUpsertRequest dto) {
        return categoryService.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar categoría", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public CategoryResponse update(@PathVariable Long id, @Valid @RequestBody CategoryUpsertRequest dto) {
        return categoryService.update(id, dto);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activar o desactivar categoría", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado de la categoría actualizado"),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public CategoryResponse toggleStatus(@PathVariable Long id) {
        return categoryService.toggleStatus(id);
    }
}
