package com.hean.consigueventas.oonabe.category.controller;

import com.hean.consigueventas.oonabe.category.dto.CategoryCreateDTO;
import com.hean.consigueventas.oonabe.category.dto.CategoryDTO;
import com.hean.consigueventas.oonabe.category.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryDTO> findActive() {
        return categoryService.findActive();
    }

    @PostMapping
    public CategoryDTO create(@Valid @RequestBody CategoryCreateDTO dto) {
        return categoryService.create(dto);
    }

    @PutMapping("/{id}")
    public CategoryDTO update(@PathVariable Long id, @Valid @RequestBody CategoryCreateDTO dto) {
        return categoryService.update(id, dto);
    }

    @PatchMapping("/{id}/status")
    public CategoryDTO toggleStatus(@PathVariable Long id) {
        return categoryService.toggleStatus(id);
    }
}
