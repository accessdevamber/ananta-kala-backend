package com.anantakala.backend.service;

import com.anantakala.backend.dto.category.CategoryPatchRequest;
import com.anantakala.backend.dto.category.CategoryRequest;
import com.anantakala.backend.dto.category.CategoryResponse;
import com.anantakala.backend.entity.Category;
import com.anantakala.backend.mapper.CategoryMapper;
import com.anantakala.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Objects;
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

//===============BELOW TWO COMMENTED METHODS ARE FOR TESTING LAZY LOADING/HIBERNATE PROXY=====================

//    @Transactional(readOnly = true)
//    public CategoryResponse findById(UUID id) {
//
//        Category category = categoryRepository.findById(id)
//                .orElseThrow();
//
//        System.out.println("STEP 1: Child category fetched");
//
//        Category parent = category.getParent();
//
//        //
//        System.out.println("Before: " + Hibernate.isInitialized(parent));
//
//        System.out.println("Parent ID: " + parent.getId());
//
//        System.out.println("After ID: " + Hibernate.isInitialized(parent));
//
//        System.out.println("Parent name: " + parent.getName());
//
//        System.out.println("After name: " + Hibernate.isInitialized(parent));
//        //
//
//        System.out.println("STEP 2: Parent proxy obtained");
//
//        if (parent != null) {
//
//            System.out.println("STEP 3: Parent ID = " + parent.getId());
//
//            System.out.println("STEP 4: Parent name = " + parent.getName());
//
//            System.out.println("STEP 5: Parent slug = " + parent.getSlug());
//        }
//
//        return categoryMapper.toResponse(category);
//    }

//    @Transactional(readOnly = true)
//    public CategoryResponse findById(UUID id) {
//
//        Category category = categoryRepository.findById(id)
//                .orElseThrow();
//
//        System.out.println("STEP 1: Child fetched");
//
//        Category parent = category.getParent();
//
//        if (parent != null) {
//
//            System.out.println("STEP 2: Parent initialized? "
//                    + Hibernate.isInitialized(parent));
//
//            System.out.println("STEP 3: Parent ID = "
//                    + parent.getId());
//
//            System.out.println("STEP 4: Parent initialized? "
//                    + Hibernate.isInitialized(parent));
//
//            System.out.println("STEP 5: Parent name = "
//                    + parent.getName());
//
//            System.out.println("STEP 6: Parent initialized? "
//                    + Hibernate.isInitialized(parent));
//        }
//
//        return categoryMapper.toResponse(category);
//    }

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


    @Transactional
    public CategoryResponse update(UUID id, CategoryRequest request) {

        Category category = getCategory(id);

        // PUT: all editable fields are replaced
        category.setName(requireText(request.name(), "name"));
        category.setSlug(requireText(request.slug(), "slug"));
        category.setDescription(request.description());

        if (request.active() == null) {
            throw badRequest("active field is required for PUT");
        }

        category.setActive(request.active());

        Category parent = resolveParent(id, request.parentId());
        category.setParent(parent);

        validateUniqueSlug(category.getSlug(), id);

        CategoryResponse response = categoryMapper.toResponse(categoryRepository.save(category));

        log.info("Updated category : {}", objectMapper.writeValueAsString(response));

        return response;
    }

    @Transactional
    public CategoryResponse patch(UUID id, CategoryPatchRequest request) {

        Category category = getCategory(id);

        if (request.name() != null) {
            category.setName(requiredJsonText(request.name(), "name"));
        }
        if (request.slug() != null) {
            category.setSlug(requiredJsonText(request.slug(), "slug"));
            validateUniqueSlug(category.getSlug(), id);
        }
        if (request.description() != null) {
            JsonNode node = request.description();
            if (node.isNull()) {
                category.setDescription(null);
            } else {
                category.setDescription(requiredJsonText(node, "description"));
            }
        }
        if (request.active() != null) {
            JsonNode node = request.active();
            if (!node.isBoolean()) {
                throw badRequest("active must be true or false");
            }
            category.setActive(node.booleanValue());
        }
        if (request.parentId() != null) {
            JsonNode node = request.parentId();
            UUID parentId = null;
            if (!node.isNull()) {
                if (!node.isTextual()) {
                    throw badRequest("parentId must be a UUID or null");
                }
                try {
                    parentId = UUID.fromString(node.textValue());
                } catch (IllegalArgumentException ex) {
                    throw badRequest("Invalid parentId UUID");
                }
            }
            category.setParent(resolveParent(id, parentId));
        }

        CategoryResponse response = categoryMapper.toResponse(categoryRepository.save(category));
        log.info("Updated category using patch : {}", objectMapper.writeValueAsString(response));
        return response;
    }

    private Category getCategory(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Category not found"
                ));
    }

    private Category resolveParent(UUID categoryId, UUID parentId) {

        if (parentId == null) {
            log.info("parentId is NULL");
            return null;
        }
        if (categoryId.equals(parentId)) {
            log.warn("A category cannot be its own parent");
            throw badRequest("A category cannot be its own parent");
        }
        Category parent = categoryRepository.findById(parentId)
                .orElseThrow(() -> {
                    log.warn("Parent category not found");
                    throw new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Parent Category not found"
                    );
                });

        // Prevent cycles such as A -> B -> C -> A
        Category current = parent;

        while (current != null) {
            if (Objects.equals(current.getId(), categoryId)) {
                log.warn("Found Circular category hierarchy");
                throw badRequest("Circular category hierarchy is not allowed");
            }
            current = current.getParent();
        }
        return parent;
    }

    private void validateUniqueSlug(String slug, UUID currentId) {
        categoryRepository.findBySlug(slug)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(currentId)) {
                        log.warn("Category slug already exists");
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Category slug already exists"
                        );
                    }
                });
    }

    //check if field value is null or blank
    private String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw badRequest(field + " must not be blank");
        }
        return value;
    }

    private String requiredJsonText(JsonNode node, String field) {
        if (!node.isTextual() || node.textValue().isBlank()) {
            throw badRequest(field + " must be a non-blank string");
        }
        return node.textValue();
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

}
