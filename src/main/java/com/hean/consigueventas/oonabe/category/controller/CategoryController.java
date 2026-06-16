package com.hean.consigueventas.oonabe.category.controller;

import com.hean.consigueventas.oonabe.category.dto.CategoryCreateDTO;
import com.hean.consigueventas.oonabe.category.dto.CategoryDTO;
import com.hean.consigueventas.oonabe.category.service.CategoryService;
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
@RequestMapping("/api/v1/category")
@Tag(name = "Categorias", description = "Catalogo de categorias de eventos.")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @Operation(summary = "Listar categorias activas", description = "Devuelve las categorias visibles para el catalogo publico.", security = {})
    @ApiResponse(responseCode = "200", description = "Categorias activas")
    public List<CategoryDTO> findActive() {
        return categoryService.findActive();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear categoria", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria creada"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public CategoryDTO create(@Valid @RequestBody CategoryCreateDTO dto) {
        return categoryService.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar categoria", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public CategoryDTO update(@PathVariable Long id, @Valid @RequestBody CategoryCreateDTO dto) {
        return categoryService.update(id, dto);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activar o desactivar categoria", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public CategoryDTO toggleStatus(@PathVariable Long id) {
        return categoryService.toggleStatus(id);
    }
}
