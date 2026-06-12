package com.hean.consigueventas.oonabe.category.mapper;

import com.hean.consigueventas.oonabe.category.dto.CategoryCreateDTO;
import com.hean.consigueventas.oonabe.category.dto.CategoryDTO;
import com.hean.consigueventas.oonabe.category.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    public CategoryDTO toDto(Category category) {
        return new CategoryDTO(category.getId(), category.getName(), category.getDescription(), category.isActive());
    }

    public Category toEntity(CategoryCreateDTO dto) {
        Category category = new Category();

        category.setName(dto.name());
        category.setDescription(dto.description());

        return category;
    }

}
