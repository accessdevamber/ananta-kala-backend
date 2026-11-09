
package com.anantakala.backend.service;

import com.anantakala.backend.dto.productvariant.ProductVariantRequest;
import com.anantakala.backend.dto.productvariant.ProductVariantResponse;
import com.anantakala.backend.entity.Product;
import com.anantakala.backend.entity.ProductVariant;
import com.anantakala.backend.enums.ProductType;
import com.anantakala.backend.mapper.ProductVariantMapper;
import com.anantakala.backend.repository.ProductRepository;
import com.anantakala.backend.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductVariantService {

    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;
    private final ProductVariantMapper variantMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public ProductVariantResponse create(ProductVariantRequest request) {

        // 1. Validate required fields
        if (request.productId() == null) {
            log.warn("productId is required");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "productId is required");
        }

        if (request.sku() == null || request.sku().isBlank()) {
            log.warn("SKU is required");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "SKU is required");
        }

        if (request.productType() == null) {
            log.warn("productType is required");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "productType is required");
        }

        if (request.price() == null ||
                request.price().compareTo(BigDecimal.ZERO) < 0) {
            log.warn("price must be >= 0");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "price must be >= 0");
        }

        // 2. Validate physical vs digital configuration
        if (request.productType() == ProductType.FRAMED_CANVAS) {

            if (request.sizeLabel() == null ||
                    request.sizeLabel().isBlank() ||
                    request.frameFinish() == null ||
                    request.frameFinish().isBlank()) {

                log.warn("FRAMED_CANVAS requires sizeLabel and frameFinish");
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "FRAMED_CANVAS requires sizeLabel and frameFinish"
                );
            }

        } else if (request.productType() == ProductType.DIGITAL_DOWNLOAD) {

            if (request.sizeLabel() != null ||
                    request.frameFinish() != null) {

                log.warn("DIGITAL_DOWNLOAD must not have sizeLabel or frameFinish");
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "DIGITAL_DOWNLOAD must not have sizeLabel or frameFinish"
                );
            }
        }

        // 3. Check SKU uniqueness
        if (variantRepository.existsBySku(request.sku())) {
            log.warn("SKU already exists");
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "SKU already exists");
        }

        String currency = request.currency() == null
                ? "INR"
                : request.currency();

        if (!currency.matches("[A-Z]{3}")) {
            log.warn("currency must be a three-letter uppercase code");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "currency must be a three-letter uppercase code"
            );
        }

        // 4. Fetch existing Product
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found"));

        // 5. Map request to entity
        ProductVariant variant = variantMapper.toEntity(request);

        // 6. Set Product relationship
        variant.setProduct(product);

        // 7. Save and return
        ProductVariant saved = variantRepository.saveAndFlush(variant);

        ProductVariantResponse response = variantMapper.toResponse(saved);
        log.info("Saved ProductVariant : {}", objectMapper.writeValueAsString(response));
        return response;
    }
}

//Why saveAndFlush()?
//We previously explored save(), flush(), and Hibernate dirty checking.
//
//Here, we're creating a new entity.
//ProductVariant saved = variantRepository.saveAndFlush(variant);
//
//
//This persists the entity and flushes pending SQL to the database before we build the response.
//
//Our @PrePersist callback generates the UUIDv7 and initializes timestamps before insertion.
//
//saveAndFlush() is not mandatory for every create operation; it's useful here to ensure the insert has been sent
// to PostgreSQL before response mapping. The transaction still commits afterward.
