package com.anantakala.backend.controller;

import com.anantakala.backend.dto.category.CategoryPatchRequest;
import com.anantakala.backend.dto.category.CategoryRequest;
import com.anantakala.backend.dto.category.CategoryResponse;
import com.anantakala.backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@RequestBody CategoryRequest request) {

        log.info("Inside create() category");
        return categoryService.create(request);
    }

    @GetMapping
    public List<CategoryResponse> findAll() {
        log.info("Inside findAll() category");
        return categoryService.findAll();
    }

    @GetMapping("/{id}")
    public CategoryResponse findById(@PathVariable UUID id) {
        log.info("Inside findById() category");
        return categoryService.findById(id);
    }

    @GetMapping("/slug/{slug}")
    public CategoryResponse findBySlug(@PathVariable String slug) {
        log.info("Inside findBySlug() category");
        return categoryService.findBySlug(slug);
    }


    @PutMapping("/{id}")
    public CategoryResponse updateCategoryObject(@PathVariable UUID id, @RequestBody CategoryRequest request) {
        log.info("Inside updateCategoryObject() category");
        return categoryService.update(id, request);
    }

    @PatchMapping("/{id}")
    public CategoryResponse updateCategoryFields(@PathVariable UUID id, @RequestBody CategoryPatchRequest request) {
        log.info("Inside updateCategoryFields() category");
        return categoryService.patch(id, request);
    }

}

