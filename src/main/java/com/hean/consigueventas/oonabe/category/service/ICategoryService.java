package com.hean.consigueventas.oonabe.category.service;

import com.hean.consigueventas.oonabe.category.dto.request.CategoryUpsertRequest;
import com.hean.consigueventas.oonabe.category.dto.response.CategoryResponse;
import java.util.List;

public interface ICategoryService {
    List<CategoryResponse> findActive();
    CategoryResponse create(CategoryUpsertRequest request);
    CategoryResponse update(Long id, CategoryUpsertRequest request);
    CategoryResponse toggleStatus(Long id);
}
