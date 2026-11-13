
package com.anantakala.backend.service;

import com.anantakala.backend.dto.product.ProductResponse;
import com.anantakala.backend.dto.productvariant.ProductVariantPatchRequest;
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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

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

    @Transactional(readOnly = true)
    public ProductVariantResponse findById(UUID id) {

        ProductVariant productVariant = variantRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product variant not found");
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Product variant not found with ID: " + id
                    );
                });
        ProductVariantResponse response = variantMapper.toResponse(productVariant);
        log.info("Fetched ProductVariant by Id : {}", objectMapper.writeValueAsString(response));
        return response;
    }

    @Transactional(readOnly = true)
    public List<ProductVariantResponse> findAll() {

        List<ProductVariantResponse> response = variantRepository.findAll()
                .stream()
                .map(variantMapper::toResponse)
                .toList();
        log.info("Fetched all ProductVariant : {}", objectMapper.writeValueAsString(response));
        return response;
    }

    @Transactional(readOnly = true)
    public ProductVariantResponse findBySku(String sku) {
        ProductVariant productVariant = variantRepository.findBySku(sku)
                .orElseThrow(() -> {
                    log.warn("Product variant not found with sku");
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Product variant not found with sku: " + sku
                    );
                });
        ProductVariantResponse response = variantMapper.toResponse(productVariant);
        log.info("Fetched ProductVariant by sku : {}", objectMapper.writeValueAsString(response));
        return response;
    }


    @Transactional(readOnly = true)
    public List<ProductVariantResponse> findByProductId(UUID productId) {

        // Verify that the parent Product exists
        if (!productRepository.existsById(productId)) {
            log.warn("Invalid productId");
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Product not found with ID: " + productId
            );
        }

        // Fetch variants belonging to this Product
        List<ProductVariantResponse> response = variantRepository.findByProduct_Id(productId)
                        .stream()
                        .map(variantMapper::toResponse)
                        .toList();

        log.info("Fetched {} variants for productId: {}", response.size(), productId);
        log.info("Fetched ProductVariant by productId : {}", objectMapper.writeValueAsString(response));
        return response;
    }


    @Transactional
    public ProductVariantResponse update(UUID id, ProductVariantRequest request) {

        // 1. Fetch existing variant
        ProductVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("ProductVariant not found with ID: {}", id);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "ProductVariant not found with ID: " + id
                    );
                });

        // 2. Validate complete PUT request
        validateForUpdate(request);

        // 3. Check SKU uniqueness (excluding current variant)

        //There are three possible situations.
        //Case 1: Keep the same SKU — allowed
        //Request:
        //{
        //  "sku": "SHIVA-BLACK"
        //}
        //Your code executes:
        //variantRepository.findBySku("SHIVA-BLACK");
        //It finds variant 111.
        //Then:
        //if (!existing.getId().equals(id))
        //becomes:
        //if (!111.equals(111))
        //Result: false.
        //So no exception is thrown. The variant is allowed to keep its existing SKU.


        //Case 2: Use another variant's SKU — rejected
        //Request:
        //{
        //  "sku": "SHIVA-GOLD"
        //}
        //The repository finds variant 222.
        //The condition becomes:
        //if (!222.equals(111))
        //Result: true.
        //So we throw:
        //throw new ResponseStatusException(
        //        HttpStatus.CONFLICT,
        //        "SKU already exists: SHIVA-GOLD"
        //);
        //The API returns 409 Conflict, because variant 222 already owns that SKU.


        //Case 3: Use a completely new SKU — allowed
        //Request:
        //{
        //  "sku": "SHIVA-SILVER"
        //}
        //The repository searches for SHIVA-SILVER, but no variant has that SKU.
        //So:
        //variantRepository.findBySku("SHIVA-SILVER");
        //returns:
        //Optional.empty()
        //Because the Optional is empty, .ifPresent(...) does not execute its lambda.
        //The update continues normally.

        variantRepository.findBySku(request.sku())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        log.warn("SKU already exists: {}", request.sku());
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "SKU already exists: " + request.sku()
                        );
                    }
                });

        // 4. Fetch Product for relationship
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found with ID: " + request.productId()
                ));

        // 5. Update editable fields using MapStruct
        variantMapper.updateEntity(request, variant);

        // 6. Set Product relationship

        //One design consideration for later: We currently allow changing a variant's productId, productType,
        // and SKU through PUT. Once variants are referenced by actual customer orders,
        // we should consider restricting changes that alter the meaning of an existing sellable item, to preserve order history.
        variant.setProduct(product);

        // 7. Flush pending SQL changes
        variantRepository.flush();

        // 8. Convert updated entity into response
        ProductVariantResponse response = variantMapper.toResponse(variant);
        log.info("Updated Product variant by Id : {}", objectMapper.writeValueAsString(response));
        return response;
    }



    @Transactional
    public ProductVariantResponse patch(UUID id, ProductVariantPatchRequest request) {

        // 1. Fetch existing variant
        ProductVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("ProductVariant not found with ID: {}", id);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "ProductVariant not found with ID: " + id
                    );
                });

        if (request == null) {
            log.warn("Request body is required");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Request body is required"
            );
        }

        // 2. Product relationship
        if (request.productId() != null) {
            JsonNode node = request.productId();

            if (!node.isTextual()) {
                log.warn("productId must be a UUID string");
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "productId must be a UUID string"
                );
            }

            UUID productId;
            try {
                productId = UUID.fromString(node.asText());
            } catch (IllegalArgumentException ex) {
                log.warn("Invalid productId UUID");
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid productId UUID"
                );
            }

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> {
                        log.warn("Product not found with ID: {}", productId);
                        return new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Product not found with ID: " + productId
                        );
                    });

            variant.setProduct(product);
        }

        // 3. SKU
        if (request.sku() != null) {
            JsonNode node = request.sku();

            if (!node.isTextual() || node.asText().isBlank()) {
                log.warn("sku must be a non-blank string");
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "sku must be a non-blank string"
                );
            }

            String sku = node.asText();

            variantRepository.findBySku(sku)
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            log.warn("SKU already exists: {}", sku);
                            throw new ResponseStatusException(
                                    HttpStatus.CONFLICT,
                                    "SKU already exists: " + sku
                            );
                        }
                    });

            variant.setSku(sku);
        }

        // 4. Product type
        if (request.productType() != null) {
            JsonNode node = request.productType();

            if (!node.isTextual()) {
                log.warn("productType must be a string");
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "productType must be a string"
                );
            }

            try {
                variant.setProductType(ProductType.valueOf(node.asText()));
            } catch (IllegalArgumentException ex) {
                log.warn("Invalid productType");
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid productType"
                );
            }
        }

        // 5. Size label (nullable)
        if (request.sizeLabel() != null) {
            JsonNode node = request.sizeLabel();

            if (node.isNull()) {
                variant.setSizeLabel(null);
            } else if (node.isTextual() && !node.asText().isBlank()) {
                variant.setSizeLabel(node.asText());
            } else {
                log.warn("sizeLabel must be a non-blank string or null");
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "sizeLabel must be a non-blank string or null"
                );
            }
        }

        // 6. Frame finish (nullable)
        if (request.frameFinish() != null) {
            JsonNode node = request.frameFinish();

            if (node.isNull()) {
                variant.setFrameFinish(null);
            } else if (node.isTextual() && !node.asText().isBlank()) {
                variant.setFrameFinish(node.asText());
            } else {
                log.warn("frameFinish must be a non-blank string or null");
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "frameFinish must be a non-blank string or null"
                );
            }
        }

        // 7. Price
        if (request.price() != null) {
            JsonNode node = request.price();

            if (!node.isNumber()) {
                log.warn("price must be a number");
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "price must be a number"
                );
            }

            BigDecimal price = new BigDecimal(node.asText());

            if (price.compareTo(BigDecimal.ZERO) < 0) {
                log.warn("price cannot be negative");
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "price cannot be negative"
                );
            }

            variant.setPrice(price);
        }

        // 8. Currency
        if (request.currency() != null) {
            JsonNode node = request.currency();

            if (!node.isTextual() ||
                    !node.asText().matches("[A-Z]{3}")) {
                log.warn("currency must be a three-letter uppercase code");
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "currency must be a three-letter uppercase code"
                );
            }

            variant.setCurrency(node.asText());
        }

        // 9. Active
        if (request.active() != null) {
            JsonNode node = request.active();

            if (!node.isBoolean()) {
                log.warn("active must be true or false");
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "active must be true or false"
                );
            }

            variant.setActive(node.booleanValue());
        }

        // 10. Validate final configuration
        validateVariantConfiguration(variant);

        // 11. Flush changes
        variantRepository.flush();

        log.info("Patched ProductVariant with ID: {}", id);

        return variantMapper.toResponse(variant);
    }


    private void validateVariantConfiguration(ProductVariant variant) {

        if (variant.getProductType() == ProductType.FRAMED_CANVAS) {

            if (variant.getSizeLabel() == null ||
                    variant.getSizeLabel().isBlank() ||
                    variant.getFrameFinish() == null ||
                    variant.getFrameFinish().isBlank()) {
                log.warn("FRAMED_CANVAS requires sizeLabel and frameFinish");
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "FRAMED_CANVAS requires sizeLabel and frameFinish"
                );
            }

        } else if (variant.getProductType() == ProductType.DIGITAL_DOWNLOAD) {

            if (variant.getSizeLabel() != null ||
                    variant.getFrameFinish() != null) {
                log.warn("DIGITAL_DOWNLOAD requires null sizeLabel and frameFinish");
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "DIGITAL_DOWNLOAD requires null sizeLabel and frameFinish"
                );
            }

        } else {
            log.warn("Invalid productType");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid productType"
            );
        }
    }

    private void validateForUpdate(ProductVariantRequest request) {

        if (request == null) {
            log.warn("Request body is required");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Request body is required");
        }

        if (request.productId() == null) {
            log.warn("productId is required");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "productId is required");
        }

        if (request.sku() == null || request.sku().isBlank()) {
            log.warn("sku is required");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "sku is required");
        }

        if (request.productType() == null) {
            log.warn("productType is required");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "productType is required");
        }

        if (request.price() == null ||
                request.price().compareTo(BigDecimal.ZERO) < 0) {
            log.warn("price must be greater than or equal to zero");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "price must be greater than or equal to zero");
        }

        if (request.currency() == null ||
                !request.currency().matches("[A-Z]{3}")) {
            log.warn("currency must be a three-letter uppercase code");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "currency must be a three-letter uppercase code");
        }

        if (request.active() == null) {
            log.warn("active is required");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "active is required");
        }

        if (request.productType() == ProductType.FRAMED_CANVAS) {

            if (request.sizeLabel() == null ||
                    request.sizeLabel().isBlank() ||
                    request.frameFinish() == null ||
                    request.frameFinish().isBlank()) {
                log.warn("FRAMED_CANVAS requires sizeLabel and frameFinish");
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "FRAMED_CANVAS requires sizeLabel and frameFinish");
            }
        }

        if (request.productType() == ProductType.DIGITAL_DOWNLOAD) {

            if (request.sizeLabel() != null ||
                    request.frameFinish() != null) {
                log.warn("DIGITAL_DOWNLOAD requires null sizeLabel and frameFinish");
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "DIGITAL_DOWNLOAD requires null sizeLabel and frameFinish");
            }
        }
    }


   /* @Transactional(readOnly = true)
    public List<ProductVariantResponse> findByProductId(UUID productId) {
//        Product product = productRepository.findById(productId)
//                .orElseThrow(() -> {
//                    log.warn("Product doesn't exist for given productId");
//                    throw new ResponseStatusException(
//                            HttpStatus.NOT_FOUND,
//                            "Product doesn't exist for given productId: " + productId
//                    );
//                });
        List<ProductVariantResponse> response = variantRepository.findByProduct_Id(productId)
                        .stream()
                        .map(variantMapper::toResponse)
                        .toList();
        if (response.isEmpty()) {
            log.warn("productId invalid. No variants exist");
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Product variants not found with productId: " + productId
            );
        }
        log.info("Fetched ProductVariant by productId : {}", objectMapper.writeValueAsString(response));
        return response;
    }*/
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



//Why JsonNode instead of String or BigDecimal?
//Consider these three requests:
//{}
//
//
//{
//  "sizeLabel": null
//}
//
//
//{
//  "sizeLabel": "16x20"
//}
//
//
//With JsonNode fields, we can distinguish all three cases:
//JSON request	Java record field	Meaning
//{}	null reference	Don't update
//{"sizeLabel":null}	NullNode	Clear value
//{"sizeLabel":"16x20"}	TextNode	Replace value
//
//This is the same technique we used for Category and Product PATCH.