package com.anantakala.backend.service;

import com.anantakala.backend.dto.cart.AddCartItemRequest;
import com.anantakala.backend.dto.cart.CartItemResponse;
import com.anantakala.backend.dto.cart.CartResponse;
import com.anantakala.backend.entity.Cart;
import com.anantakala.backend.entity.CartItem;
import com.anantakala.backend.entity.ProductVariant;
import com.anantakala.backend.enums.CartStatus;
import com.anantakala.backend.enums.ProductStatus;
import com.anantakala.backend.mapper.CartItemMapper;
import com.anantakala.backend.mapper.CartMapper;
import com.anantakala.backend.mapper.ProductVariantMapper;
import com.anantakala.backend.repository.CartItemRepository;
import com.anantakala.backend.repository.CartRepository;
import com.anantakala.backend.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final ObjectMapper objectMapper;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CartItemMapper cartItemMapper;
    private final ProductVariantMapper variantMapper;

    @Transactional
    public CartResponse create() {

        // 1. Create an empty Cart
        Cart cart = new Cart();

        // 2. Set initial status
        cart.setStatus(CartStatus.ACTIVE);

        // 3. Persist Cart
        Cart savedCart = cartRepository.saveAndFlush(cart);

        //log.info("Created Cart with ID: {}", savedCart.getId());

        // 4. Convert entity to response DTO
        CartResponse response = cartMapper.toResponse(savedCart);
        log.info("Created Cart : {}", objectMapper.writeValueAsString(response));
        return response;
    }


    @Transactional(readOnly = true)
    public CartResponse getById(UUID cartId) {

        // 1. Fetch Cart from PostgreSQL
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> {
                    log.warn("Cart not found with ID: {}", cartId);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Cart not found with ID: " + cartId
                    );
                });

        // 2. Convert Cart entity to DTO
        CartResponse response = cartMapper.toResponse(cart);
        log.info("Fetched Cart : {}", objectMapper.writeValueAsString(response));
        return response;
    }


    @Transactional
    public CartItemResponse addItem(
            UUID cartId,
            AddCartItemRequest request
    ) {

        // 1. Find the Cart
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> {
                    log.warn("Cart not found with ID: {}", cartId);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Cart not found with ID: " + cartId
                    );
                });

        // 2. Only ACTIVE carts can be modified
        if (cart.getStatus() != CartStatus.ACTIVE) {
            log.warn("Cannot add items to a non-active cart");
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot add items to a non-active cart"
            );
        }

        // 3. Find the ProductVariant
        ProductVariant variant = productVariantRepository
                .findById(request.productVariantId())
                .orElseThrow(() -> {
                    log.warn("ProductVariant not found with ID: {}", request.productVariantId());
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "ProductVariant not found with ID: "
                                    + request.productVariantId()
                    );
                });
        log.info("Adding productVariant : {}", objectMapper.writeValueAsString(variantMapper.toResponse(variant)));

        // 4. Check whether the variant is available
        if (!variant.isActive()) {
            log.warn("ProductVariant is inactive");
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "ProductVariant is inactive"
            );
        }

        if (variant.getProduct().getStatus() != ProductStatus.ACTIVE) {
            log.warn("Product is not active");
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product is not active"
            );
        }

        // 5. Find an existing CartItem for this variant
        Optional<CartItem> existingItem = cartItemRepository
                .findByCart_IdAndProductVariant_Id(
                        cartId,
                        request.productVariantId()
                );

        //variable to store final saved cartItem
        CartItem savedItem;

        if (existingItem.isPresent()) {

            log.info("productVariant already exists in the existing cart. Increasing quantity");
            // Same variant already exists: increase quantity
            CartItem item = existingItem.get();

            int newQuantity;

            try {
                newQuantity = Math.addExact(
                        item.getQuantity(),
                        request.quantity()
                );
            } catch (ArithmeticException ex) {
                log.warn("Quantity is too large");
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Quantity is too large"
                );
            }

            item.setQuantity(newQuantity);//set new quantity

            savedItem = cartItemRepository.saveAndFlush(item);

        } else {

            log.info("productVariant doesn't exist in the existing cart. Adding new productVariant to cart");
            // New variant: create a CartItem
            CartItem item = new CartItem();

            item.setCart(cart);
            item.setProductVariant(variant);
            item.setQuantity(request.quantity());

            savedItem = cartItemRepository.saveAndFlush(item);
        }

        // 6. Calculate price using ProductVariant
        BigDecimal unitPrice = variant.getPrice();

        BigDecimal lineTotal = unitPrice.multiply(
                BigDecimal.valueOf(savedItem.getQuantity())
        );

        // 7. Convert to response DTO
        CartItemResponse response = cartItemMapper.toResponse(
                savedItem,
                unitPrice,
                lineTotal
        );

        log.info("Created cartItem for cartId {} : {}", cartId, response);
        return response;
    }

}

//Why saveAndFlush()?
//- save() registers the new Cart for persistence.
//- flush() executes the pending SQL INSERT before returning.
//- The transaction commits when the transactional service call completes successfully.
//We could also use save() alone because Hibernate automatically flushes before transaction commit.
// saveAndFlush() is useful here for observing the INSERT while learning.
//
//Also, your Cart entity's @PrePersist method generates the UUIDv7 and timestamps.


//Why @Transactional(readOnly = true)?
//This tells Spring that the method is intended for reading data, not modifying it.
//
//It also keeps the persistence context available while the method executes.
//
//It's a useful convention for GET operations.
//
//Note that readOnly = true is an optimization hint, not a database security restriction.

