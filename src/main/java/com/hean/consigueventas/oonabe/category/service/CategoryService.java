package com.hean.consigueventas.oonabe.category.service;

import com.hean.consigueventas.oonabe.category.dto.request.CategoryUpsertRequest;
import com.hean.consigueventas.oonabe.category.dto.response.CategoryResponse;
import com.hean.consigueventas.oonabe.category.entity.Category;
import com.hean.consigueventas.oonabe.category.mapper.CategoryMapper;
import com.hean.consigueventas.oonabe.category.repository.CategoryRepository;
import com.hean.consigueventas.oonabe.community.repository.MatchRequestRepository;
import com.hean.consigueventas.oonabe.event.repository.EventRepository;
import com.hean.consigueventas.oonabe.profileCliente.repository.ClientInterestCategoryRepository;
import com.hean.consigueventas.oonabe.common.exception.BusinessLogicException;
import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final EventRepository eventRepository;
    private final MatchRequestRepository matchRequestRepository;
    private final ClientInterestCategoryRepository clientInterestCategoryRepository;

    public CategoryService(
            CategoryRepository categoryRepository,
            CategoryMapper categoryMapper,
            EventRepository eventRepository,
            MatchRequestRepository matchRequestRepository,
            ClientInterestCategoryRepository clientInterestCategoryRepository) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.eventRepository = eventRepository;
        this.matchRequestRepository = matchRequestRepository;
        this.clientInterestCategoryRepository = clientInterestCategoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> findActive() {
        return categoryRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAllByOrderByNameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse create(CategoryUpsertRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new BusinessLogicException("La categoria ya existe.");
        }

        Category category = categoryMapper.toEntity(request);
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryUpsertRequest request) {
        Category category = findById(id);
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(request.name(), id)) {
            throw new BusinessLogicException("La categoria ya existe.");
        }

        category.setName(request.name());
        category.setDescription(request.description());
        category.setEmoji(request.emoji());
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse toggleStatus(Long id) {
        Category category = findById(id);
        category.setActive(!category.isActive());
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        Category category = findById(id);
        if (isInUse(id)) {
            throw new BusinessLogicException("La categoria esta en uso. Desactivala en lugar de eliminarla.");
        }
        categoryRepository.delete(category);
    }

    private Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada con ID: " + id));
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(), category.getName(), category.getSlug(), category.getDescription(),
                category.getEmoji(), category.isActive(), !isInUse(category.getId()));
    }

    private boolean isInUse(Long id) {
        return eventRepository.existsByCategoryId(id)
                || matchRequestRepository.existsByCategoriesId(id)
                || clientInterestCategoryRepository.existsByCategoryId(id);
    }
}
