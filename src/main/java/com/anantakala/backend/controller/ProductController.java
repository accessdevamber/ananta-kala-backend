
package com.anantakala.backend.controller;

import com.anantakala.backend.dto.product.ProductRequest;
import com.anantakala.backend.dto.product.ProductResponse;
import com.anantakala.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@RequestBody ProductRequest request) {
        log.info("Inside create() product");
        return productService.create(request);
    }

    @GetMapping("/{id}")
    public ProductResponse findById(@PathVariable UUID id) {
        log.info("Inside findById() product");
        return productService.findById(id);
    }


    @GetMapping
    public List<ProductResponse> findAll() {
        log.info("Inside findAll() product");
        return productService.findAll();
    }


    @GetMapping("/slug/{slug}")
    public ProductResponse findBySlug(@PathVariable String slug) {
        log.info("Inside findBySlug() product");
        return productService.findBySlug(slug);
    }



}
