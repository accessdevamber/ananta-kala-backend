package com.anantakala.backend.mapper;

import com.anantakala.backend.dto.category.CategoryRequest;
import com.anantakala.backend.dto.category.CategoryResponse;
import com.anantakala.backend.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", expression = "java(request.active() == null || request.active())")
    Category toEntity(CategoryRequest request);

    @Mapping(target = "parentId", source = "parent.id")
    CategoryResponse toResponse(Category category);
}
