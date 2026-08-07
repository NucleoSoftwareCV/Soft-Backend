package com.hean.consigueventas.oonabe.category.mapper;

import com.hean.consigueventas.oonabe.category.dto.request.CategoryUpsertRequest;
import com.hean.consigueventas.oonabe.category.dto.response.CategoryResponse;
import com.hean.consigueventas.oonabe.category.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "deletable", constant = "false")
    CategoryResponse toDto(Category category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "active", ignore = true)
    Category toEntity(CategoryUpsertRequest dto);
}
