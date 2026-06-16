package com.hean.consigueventas.oonabe.category.service;

import com.hean.consigueventas.oonabe.category.dto.CategoryCreateDTO;
import com.hean.consigueventas.oonabe.category.dto.CategoryDTO;
import com.hean.consigueventas.oonabe.category.entity.Category;
import com.hean.consigueventas.oonabe.category.mapper.CategoryMapper;
import com.hean.consigueventas.oonabe.category.repository.CategoryRepository;
import com.hean.consigueventas.oonabe.common.exception.BusinessLogicException;
import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> findActive() {
        return categoryRepository.findByActiveTrueOrderByNameAsc().stream().map(categoryMapper::toDto).toList();
    }

    @Transactional
    public CategoryDTO create(CategoryCreateDTO dto) {

        if(categoryRepository.existsByNameIgnoreCase(dto.name())) {
            throw new BusinessLogicException("La categoria ya existe.");
        }

        Category category = categoryMapper.toEntity(dto);

        Category created = categoryRepository.save(category);

        return categoryMapper.toDto(created);
    }

    @Transactional
    public CategoryDTO update(Long id, CategoryCreateDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Categoria no encontrada con ID: " + id));

        if(categoryRepository.existsByNameIgnoreCaseAndIdNot(dto.name(), id)) {
            throw new BusinessLogicException("La categoria ya existe.");
        }

        category.setName(dto.name());
        category.setDescription(dto.description());

        Category updated = categoryRepository.save(category);

        return categoryMapper.toDto(updated);
    }

    @Transactional
    public CategoryDTO toggleStatus(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada con ID: " + id));
        category.setActive(!category.isActive());

        Category updated = categoryRepository.save(category);

        return categoryMapper.toDto(updated);

    }
}
