package com.anantakala.backend.service;

import com.anantakala.backend.dto.productimage.ProductImageRequest;
import com.anantakala.backend.dto.productimage.ProductImageResponse;
import com.anantakala.backend.entity.Product;
import com.anantakala.backend.entity.ProductImage;
import com.anantakala.backend.mapper.ProductImageMapper;
import com.anantakala.backend.repository.ProductImageRepository;
import com.anantakala.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductImageService {

    private final ProductImageRepository imageRepository;
    private final ProductRepository productRepository;
    private final ProductImageMapper imageMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public ProductImageResponse create(ProductImageRequest request) {

        // 1. Validate request
        if (request == null || request.productId() == null) {
            log.warn("productId is required");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "productId is required"
            );
        }

        if (request.imageUrl() == null || request.imageUrl().isBlank()) {
            log.warn("imageUrl is required");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "imageUrl is required"
            );
        }

        if (request.sortOrder() != null && request.sortOrder() < 0) {
            log.warn("sortOrder cannot be negative");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "sortOrder cannot be negative"
            );
        }

        if (request.altText() != null && request.altText().length() > 255) {
            log.warn("altText cannot exceed 255 characters");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "altText cannot exceed 255 characters"
            );
        }

        // 2. Find parent Product
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> {
                    log.warn("Product not found with ID: {}", request.productId());
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Product not found with ID: " + request.productId()
                    );
                });

        // 3. Check primary-image uniqueness
        if (Boolean.TRUE.equals(request.primary()) &&
                imageRepository.existsByProductIdAndPrimaryTrue(
                        request.productId())) {
            log.warn("Product already has a primary image");
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product already has a primary image"
            );
        }

        // 4. Map request to entity
        ProductImage image = imageMapper.toEntity(request);

        // 5. Set relationship
        image.setProduct(product);

        // 6. Save and flush
        ProductImage saved = imageRepository.saveAndFlush(image);

        ///log.info("Created ProductImage with ID: {}", saved.getId());

        ProductImageResponse response = imageMapper.toResponse(saved);
        log.info("Saved product : {}", objectMapper.writeValueAsString(response));
        return response;
    }
}

//We use saveAndFlush() as we did for ProductVariant POST, so Hibernate executes the INSERT and the
// generated UUID/timestamps are available before returning the response.
//
//The database's unique index remains the final safeguard if two concurrent requests attempt to create primary images.

