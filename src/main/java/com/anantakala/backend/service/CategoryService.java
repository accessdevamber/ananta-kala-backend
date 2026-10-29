package com.anantakala.backend.service;

import com.anantakala.backend.dto.category.CategoryRequest;
import com.anantakala.backend.dto.category.CategoryResponse;
import com.anantakala.backend.entity.Category;
import com.anantakala.backend.mapper.CategoryMapper;
import com.anantakala.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public CategoryResponse create(CategoryRequest request) {

        if (request.name() == null || request.name().isBlank()) {
            log.warn("category name is NULL or blank");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Category name is required"
            );
        }

        if (request.slug() == null || request.slug().isBlank()) {
            log.warn("category slug is NULL or blank");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Category slug is required"
            );
        }

        if (categoryRepository.existsBySlug(request.slug())) {
            log.warn("category slug already exists");
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Category slug already exists"
            );
        }

        Category category = categoryMapper.toEntity(request);

        if (request.parentId() != null) {
            Category parent = categoryRepository
                    .findById(request.parentId())
                            .orElseThrow(() -> {
                                log.warn("Parent category not found");
                                throw new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Parent category not found");
                            });

            category.setParent(parent);
        }

        Category saved = categoryRepository.save(category);
        CategoryResponse response = categoryMapper.toResponse(saved);
        log.info("category created : {}", response);
        log.info("Saved category : {}", objectMapper.writeValueAsString(response));
        return response;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        List<CategoryResponse> categoriesList = categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
        log.info("category list : {}", categoriesList);
        return categoriesList;
    }

    @Transactional(readOnly = true)
    public CategoryResponse findById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Category not found for given id");
                    throw new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Category not found"
                    );
                });
        CategoryResponse response = categoryMapper.toResponse(category);
        log.info("category found by id : {}", response);
        return response;
    }

    @Transactional(readOnly = true)
    public CategoryResponse findBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> {
                    log.warn("Category not found for given slug");
                    throw new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Category not found"
                    );
                });
        CategoryResponse response = categoryMapper.toResponse(category);
        log.info("category found by slug : {}", response);
        return response;
    }
}
