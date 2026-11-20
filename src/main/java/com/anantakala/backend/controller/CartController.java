package com.anantakala.backend.controller;

import com.anantakala.backend.dto.cart.AddCartItemRequest;
import com.anantakala.backend.dto.cart.CartItemResponse;
import com.anantakala.backend.dto.cart.CartResponse;
import com.anantakala.backend.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    @PostMapping
    public ResponseEntity<CartResponse> create() {

        CartResponse response = cartService.create();

        log.info("Cart created with ID: {}", response.id());

        return ResponseEntity
                .created(URI.create("/api/carts/" + response.id()))
                .body(response);
    }


    @GetMapping("/{id}")
    public ResponseEntity<CartResponse> getById(
            @PathVariable UUID id
    ) {

        CartResponse response = cartService.getById(id);

        log.info("Fetched Cart with ID: {}", id);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartItemResponse> addItem(
            @PathVariable UUID cartId,
            @Valid @RequestBody AddCartItemRequest request
    ) {

        CartItemResponse response = cartService.addItem(cartId, request);

        log.info("CartItem added/updated. Cart ID: {}, Item ID: {}", cartId, response.id());

        return ResponseEntity.ok(response);
    }


}

