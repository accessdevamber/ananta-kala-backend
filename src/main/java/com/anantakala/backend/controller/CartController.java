package com.anantakala.backend.controller;

import com.anantakala.backend.dto.cart.AddCartItemRequest;
import com.anantakala.backend.dto.cart.CartItemResponse;
import com.anantakala.backend.dto.cart.CartResponse;
import com.anantakala.backend.dto.cart.UpdateCartItemRequest;
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


    @PatchMapping("/{cartId}/items/{itemId}")
    public ResponseEntity<CartItemResponse> updateItemQuantity(
            @PathVariable UUID cartId,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {

        CartItemResponse response = cartService.updateItemQuantity(
                cartId,
                itemId,
                request
        );

        log.info(
                "Updated CartItem quantity. Cart ID: {}, Item ID: {}, Quantity: {}",
                cartId,
                itemId,
                response.quantity()
        );

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{cartId}/items/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable UUID cartId,
            @PathVariable UUID itemId
    ) {

        cartService.deleteItem(cartId, itemId);

        log.info("CartItem deletion completed. Cart ID: {}, Item ID: {}", cartId, itemId);

        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/{cartId}/items")
    public ResponseEntity<Void> clearCart(
            @PathVariable UUID cartId
    ) {

        cartService.clearCart(cartId);

        log.info("Cart cleared successfully: {}", cartId);

        return ResponseEntity.noContent().build();
    }


}

