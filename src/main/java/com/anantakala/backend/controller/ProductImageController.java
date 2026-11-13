package com.anantakala.backend.controller;


import com.anantakala.backend.dto.productimage.ProductImageRequest;
import com.anantakala.backend.dto.productimage.ProductImageResponse;
import com.anantakala.backend.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
}

