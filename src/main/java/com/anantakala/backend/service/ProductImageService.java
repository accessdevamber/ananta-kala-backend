package com.anantakala.backend.service;

import com.anantakala.backend.dto.product.ProductResponse;
import com.anantakala.backend.dto.productimage.ProductImagePatchRequest;
import com.anantakala.backend.dto.productimage.ProductImageRequest;
import com.anantakala.backend.dto.productimage.ProductImageResponse;
import com.anantakala.backend.dto.productvariant.ProductVariantResponse;
import com.anantakala.backend.entity.Product;
import com.anantakala.backend.entity.ProductImage;
import com.anantakala.backend.enums.ImageType;
import com.anantakala.backend.mapper.ProductImageMapper;
import com.anantakala.backend.repository.ProductImageRepository;
import com.anantakala.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

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

    @Transactional(readOnly = true)
    public ProductImageResponse findById(UUID id) {

        ProductImage productImage = imageRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product image not found");
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Product image not found with ID: " + id
                    );
                });
        ProductImageResponse response = imageMapper.toResponse(productImage);
        log.info("Fetched product image by Id : {}", objectMapper.writeValueAsString(response));
        return response;
    }

    @Transactional(readOnly = true)
    public List<ProductImageResponse> findAll() {

        List<ProductImageResponse> response = imageRepository.findAll()
                .stream()
                .map(imageMapper::toResponse)
                .toList();
        log.info("Fetched all product images : {}", objectMapper.writeValueAsString(response));
        return response;
    }

    @Transactional(readOnly = true)
    public List<ProductImageResponse> findImagesByProductId(UUID productId) {

        // Verify that the parent Product exists
        if (!productRepository.existsById(productId)) {
            log.warn("Invalid productId");
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Product not found with ID: " + productId
            );
        }

        // Fetch images belonging to this Product
        List<ProductImageResponse> response = imageRepository.findByProduct_IdOrderBySortOrderAscIdAsc(productId)
                .stream()
                .map(imageMapper::toResponse)
                .toList();

        log.info("Fetched {} images for productId: {}", response.size(), productId);
        log.info("Fetched ProductImage by productId : {}", objectMapper.writeValueAsString(response));
        return response;
    }


    @Transactional
    public ProductImageResponse update(
            UUID id,
            ProductImageRequest request
    ) {

        // 1. Find existing image
        ProductImage image = imageRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product image not found with ID: {}", id);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Product image not found with ID: " + id
                    );
                });

        // 2. Validate complete PUT request
        if (request == null ||
                request.productId() == null ||
                request.imageUrl() == null ||
                request.imageUrl().isBlank() ||
                request.imageType() == null ||
                request.sortOrder() == null ||
                request.primary() == null) {

            log.warn("productId, imageUrl, imageType, sortOrder and primary are required");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "productId, imageUrl, imageType, sortOrder and primary are required"
            );
        }

        if (request.sortOrder() < 0) {
            log.warn("sortOrder cannot be negative");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "sortOrder cannot be negative"
            );
        }

        if (request.altText() != null &&
                request.altText().length() > 255) {
            log.warn("altText cannot exceed 255 characters");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "altText cannot exceed 255 characters"
            );
        }

        // 3. Find parent Product
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> {
                    log.warn("Product not found with ID: {}", request.productId());
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Product not found with ID: " + request.productId()
                    );
                });

        // 4. Validate primary-image uniqueness
        if (Boolean.TRUE.equals(request.primary()) &&
                imageRepository.existsByProduct_IdAndPrimaryTrueAndIdNot(
                        request.productId(),
                        id
                )) {

            log.warn("Another primary image already exists for this product");
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Another primary image already exists for this product"
            );
        }

        // 5. Update existing managed entity
        imageMapper.updateEntity(request, image);

        // 6. Update Product relationship
        image.setProduct(product);

        // 7. Flush changes
        imageRepository.flush();

        log.info("Updated ProductImage with ID: {}", id);

        ProductImageResponse response = imageMapper.toResponse(image);
        log.info("Updated ProductImage by Id : {}", objectMapper.writeValueAsString(response));
        return response;
        //Why don't we call save()?
        //Because findById() retrieves a managed entity inside the transaction.
        //
        //When MapStruct changes its fields, Hibernate detects those changes through dirty checking.
        //
        //Calling flush() executes the pending SQL UPDATE, if the entity is dirty, and triggers @PreUpdate.
        //
        //If no values actually change, Hibernate may not execute an UPDATE, so updatedAt may remain unchanged.
    }



    @Transactional
    public ProductImageResponse patch(
            UUID id,
            ProductImagePatchRequest request
    ) {

        // 1. Find existing ProductImage
        ProductImage image = imageRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product image not found with ID: {}", id);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Product image not found with ID: " + id
                    );
                });

        if (request == null) {
            log.warn("Request body is required");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Request body is required"
            );
        }

        // 2. Calculate final productId and primary values
        // Do NOT modify the managed entity yet
        UUID finalProductId = image.getProduct().getId();
        boolean finalPrimary = image.isPrimary();

        // 3. Validate requested productId
        if (request.productId() != null) {

            JsonNode node = request.productId();

            if (!node.isTextual()) {
                log.warn("productId must be a UUID string");
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "productId must be a UUID string"
                );
            }

            try {
                finalProductId = UUID.fromString(node.asText());
            } catch (IllegalArgumentException ex) {
                log.warn("Invalid productId UUID");
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid productId UUID"
                );
            }
        }

        // 4. Validate requested primary
        if (request.primary() != null) {

            JsonNode node = request.primary();

            if (!node.isBoolean()) {
                log.warn("primary must be true or false");
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "primary must be true or false"
                );
            }

            finalPrimary = node.booleanValue();
        }

        // 5. Verify requested Product exists
        // Fetch only when productId is supplied
        Product product = null;

        if (request.productId() != null) {

            // Effectively final variable for lambda
            UUID productIdToLookup = finalProductId;

            product = productRepository.findById(productIdToLookup)
                    .orElseThrow(() -> {
                        log.warn("Product not found with ID: {}",
                                productIdToLookup);

                        return new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Product not found with ID: " + productIdToLookup
                        );
                    });
        }

        // 6. Validate primary-image uniqueness BEFORE modifying entity
        if (finalPrimary &&
                imageRepository.existsByProduct_IdAndPrimaryTrueAndIdNot(
                        finalProductId,
                        id
                )) {

            log.warn(
                    "Another primary image already exists for productId: {}",
                    finalProductId
            );

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Another primary image already exists for this product"
            );
        }

        // 7. Apply Product relationship change
        if (product != null) {
            image.setProduct(product);
        }

        // 8. Apply primary change
        if (request.primary() != null) {
            image.setPrimary(finalPrimary);
        }

        // 9. Update imageUrl
        if (request.imageUrl() != null) {

            JsonNode node = request.imageUrl();

            if (!node.isTextual() || node.asText().isBlank()) {
                log.warn("imageUrl must be a non-blank string");
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "imageUrl must be a non-blank string"
                );
            }

            image.setImageUrl(node.asText());
        }

        // 10. Update imageType
        if (request.imageType() != null) {

            JsonNode node = request.imageType();

            if (!node.isTextual()) {
                log.warn("imageType must be a string");
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "imageType must be a string"
                );
            }

            try {
                image.setImageType(
                        ImageType.valueOf(node.asText())
                );
            } catch (IllegalArgumentException ex) {
                log.warn("Invalid imageType. Allowed: MAIN, GALLERY, MOCKUP");
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid imageType. Allowed: MAIN, GALLERY, MOCKUP"
                );
            }
        }

        // 11. Update altText (nullable)
        if (request.altText() != null) {

            JsonNode node = request.altText();

            if (node.isNull()) {
                image.setAltText(null);

            } else if (node.isTextual()) {

                if (node.asText().length() > 255) {
                    log.warn("altText cannot exceed 255 characters");
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "altText cannot exceed 255 characters"
                    );
                }

                image.setAltText(node.asText());

            } else {
                log.warn("altText must be a string or null");
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "altText must be a string or null"
                );
            }
        }

        // 12. Update sortOrder
        if (request.sortOrder() != null) {

            JsonNode node = request.sortOrder();

            if (!node.isIntegralNumber() || !node.canConvertToInt()) {
                log.warn("sortOrder must be an integer");
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "sortOrder must be an integer"
                );
            }

            int sortOrder = node.intValue();

            if (sortOrder < 0) {
                log.warn("sortOrder cannot be negative");
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "sortOrder cannot be negative"
                );
            }

            image.setSortOrder(sortOrder);
        }

        // 13. Flush changes
        // Hibernate dirty checking performs UPDATE if values changed
        imageRepository.flush();

        log.info("Patched ProductImage with ID: {}", id);

        // 14. Convert entity to response DTO
        ProductImageResponse response = imageMapper.toResponse(image);

        log.info(
                "Patched ProductImage response: {}",
                objectMapper.writeValueAsString(response)
        );

        return response;
    }



}

//We use saveAndFlush() as we did for ProductVariant POST, so Hibernate executes the INSERT and the
// generated UUID/timestamps are available before returning the response.
//
//The database's unique index remains the final safeguard if two concurrent requests attempt to create primary images.

