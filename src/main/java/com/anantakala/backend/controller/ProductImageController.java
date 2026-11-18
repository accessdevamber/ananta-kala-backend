package com.anantakala.backend.controller;


import com.anantakala.backend.dto.product.ProductResponse;
import com.anantakala.backend.dto.productimage.ProductImagePatchRequest;
import com.anantakala.backend.dto.productimage.ProductImageRequest;
import com.anantakala.backend.dto.productimage.ProductImageResponse;
import com.anantakala.backend.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/product-images")
@RequiredArgsConstructor
@Slf4j
public class ProductImageController {

    private final ProductImageService imageService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductImageResponse create(
            @RequestBody ProductImageRequest request
    ) {
        log.info("Inside create() ProductImage");
        return imageService.create(request);
    }

    @GetMapping("/{id}")
    public ProductImageResponse findById(@PathVariable UUID id) {
        log.info("Inside findById() ProductImage");
        return imageService.findById(id);
    }

    @GetMapping
    public List<ProductImageResponse> findAll() {
        log.info("Inside findAll() ProductImage");
        return imageService.findAll();
    }


    @PutMapping("/{id}")
    public ProductImageResponse update(
            @PathVariable UUID id,
            @RequestBody ProductImageRequest request
    ) {
        log.info("Inside update() ProductImage, id: {}", id);
        return imageService.update(id, request);
    }


    @PatchMapping("/{id}")
    public ProductImageResponse patch(
            @PathVariable UUID id,
            @RequestBody ProductImagePatchRequest request
    ) {
        log.info("Inside patch() ProductImage, id: {}", id);
        return imageService.patch(id, request);
    }


}

