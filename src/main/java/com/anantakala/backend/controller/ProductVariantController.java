
package com.anantakala.backend.controller;

import com.anantakala.backend.dto.product.ProductResponse;
import com.anantakala.backend.dto.productvariant.ProductVariantPatchRequest;
import com.anantakala.backend.dto.productvariant.ProductVariantRequest;
import com.anantakala.backend.dto.productvariant.ProductVariantResponse;
import com.anantakala.backend.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/product-variants")
@RequiredArgsConstructor
@Slf4j
public class ProductVariantController {

    private final ProductVariantService variantService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductVariantResponse create(@RequestBody ProductVariantRequest request) {
        log.info("Inside create() ProductVariant");
        return variantService.create(request);
    }

    @GetMapping("/{id}")
    public ProductVariantResponse findById(@PathVariable UUID id) {
        log.info("Inside findById() ProductVariant");
        return variantService.findById(id);
    }

    @GetMapping
    public List<ProductVariantResponse> findAll() {
        log.info("Inside findAll() ProductVariant");
        return variantService.findAll();
    }

    @GetMapping("/sku/{sku}")
    public ProductVariantResponse findBySKU(@PathVariable String sku) {
        log.info("Inside findBySKU() ProductVariant");
        return variantService.findBySku(sku);
    }


    @PutMapping("/{id}")
    public ProductVariantResponse update(
            @PathVariable UUID id,
            @RequestBody ProductVariantRequest request
    ) {
        log.info("Inside update() ProductVariant, id: {}", id);
        return variantService.update(id, request);
    }


    @PatchMapping("/{id}")
    public ProductVariantResponse patch(
            @PathVariable UUID id,
            @RequestBody ProductVariantPatchRequest request
    ) {
        log.info("Inside patch() ProductVariant, id: {}", id);
        return variantService.patch(id, request);
    }


}
