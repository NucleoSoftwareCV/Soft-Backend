package com.hean.consigueventas.oonabe.category.service.impl;

import com.hean.consigueventas.oonabe.category.service.ICategoryService;

import com.hean.consigueventas.oonabe.category.dto.request.CategoryUpsertRequest;
import com.hean.consigueventas.oonabe.category.dto.response.CategoryResponse;
import com.hean.consigueventas.oonabe.category.entity.Category;
import com.hean.consigueventas.oonabe.category.mapper.CategoryMapper;
import com.hean.consigueventas.oonabe.category.repository.CategoryRepository;
import com.hean.consigueventas.oonabe.common.exception.BusinessLogicException;
import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryServiceImpl implements ICategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Transactional(readOnly = true)
    @Override
    public List<CategoryResponse> findActive() {
        return categoryRepository.findByActiveTrueOrderByNameAsc().stream().map(categoryMapper::toDto).toList();
    }

    @Transactional
    @Override
    public CategoryResponse create(CategoryUpsertRequest dto) {

        if(categoryRepository.existsByNameIgnoreCase(dto.name())) {
            throw new BusinessLogicException("La categoría ya existe.");
        }

        Category category = categoryMapper.toEntity(dto);

        Category created = categoryRepository.save(category);

        return categoryMapper.toDto(created);
    }

    @Transactional
    @Override
    public CategoryResponse update(Long id, CategoryUpsertRequest dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Categoría no encontrada con ID: " + id));

        if(categoryRepository.existsByNameIgnoreCaseAndIdNot(dto.name(), id)) {
            throw new BusinessLogicException("La categoría ya existe.");
        }

        category.setName(dto.name());
        category.setDescription(dto.description());

        Category updated = categoryRepository.save(category);

        return categoryMapper.toDto(updated);
    }

    @Transactional
    @Override
    public CategoryResponse toggleStatus(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));
        category.setActive(!category.isActive());

        Category updated = categoryRepository.save(category);

        return categoryMapper.toDto(updated);

    }
}
