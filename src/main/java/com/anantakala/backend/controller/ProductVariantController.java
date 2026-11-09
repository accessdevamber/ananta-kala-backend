
package com.anantakala.backend.controller;

import com.anantakala.backend.dto.productvariant.ProductVariantRequest;
import com.anantakala.backend.dto.productvariant.ProductVariantResponse;
import com.anantakala.backend.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product-variants")
@RequiredArgsConstructor
@Slf4j
public class ProductVariantController {

    private final ProductVariantService variantService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductVariantResponse create(
            @RequestBody ProductVariantRequest request
    ) {
        log.info("Inside create() ProductVariant");
        return variantService.create(request);
    }
}
