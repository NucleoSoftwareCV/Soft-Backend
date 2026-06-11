package com.hean.consigueventas.oonabe.category.controller;

import com.hean.consigueventas.oonabe.category.dto.CategoryDTO;
import com.hean.consigueventas.oonabe.category.service.CategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
