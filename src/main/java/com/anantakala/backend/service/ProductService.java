
package com.anantakala.backend.service;

import com.anantakala.backend.dto.product.ProductRequest;
import com.anantakala.backend.dto.product.ProductResponse;
import com.anantakala.backend.entity.Category;
import com.anantakala.backend.entity.Product;
import com.anantakala.backend.mapper.ProductMapper;
import com.anantakala.backend.repository.CategoryRepository;
import com.anantakala.backend.repository.ProductRepository;
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
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public ProductResponse create(ProductRequest request) {

        if (request.categoryId() == null) {
            log.warn("product's category Id is NULL or blank");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "categoryId is required"
            );
        }

        if (request.name() == null || request.name().isBlank()) {
            log.warn("product name is NULL or blank");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product name is required"
            );
        }

        if (request.slug() == null || request.slug().isBlank()) {
            log.warn("product slug is NULL or blank");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product slug is required"
            );
        }

        if (productRepository.existsBySlug(request.slug())) {
            log.warn("product slug already exists");
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product slug already exists"
            );
        }

        Category category = categoryRepository
                .findById(request.categoryId())
                .orElseThrow(() -> {
                    log.warn("Product's category not found");
                    throw new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Product's category not found"
                    );
        });

        Product product = productMapper.toEntity(request);

        product.setCategory(category);

        Product saved = productRepository.save(product);

        ProductResponse response = productMapper.toResponse(saved);
        log.info("Saved product : {}", objectMapper.writeValueAsString(response));
        return response;
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(UUID id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found");
                    throw new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Product not found with ID: " + id
                    );
                });
        ProductResponse response = productMapper.toResponse(product);
        log.info("Fetched product by Id : {}", objectMapper.writeValueAsString(response));
        return response;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {

        List<ProductResponse> response = productRepository.findAll()
                .stream()
                .map(productMapper::toResponse)
                .toList();
        log.info("Fetched all product : {}", objectMapper.writeValueAsString(response));
        return response;
    }

    @Transactional(readOnly = true)
    public ProductResponse findBySlug(String slug) {

        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> {
                    log.warn("Product not found");
                    throw new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Product not found with slug: " + slug
                    );
                });

        ProductResponse response = productMapper.toResponse(product);
        log.info("Fetched product by slug : {}", objectMapper.writeValueAsString(response));
        return response;
    }

}
