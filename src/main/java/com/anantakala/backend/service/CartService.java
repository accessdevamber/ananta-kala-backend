package com.anantakala.backend.service;

import com.anantakala.backend.dto.cart.AddCartItemRequest;
import com.anantakala.backend.dto.cart.CartItemResponse;
import com.anantakala.backend.dto.cart.CartResponse;
import com.anantakala.backend.dto.cart.UpdateCartItemRequest;
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
import java.util.ArrayList;
import java.util.List;
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
        CartResponse response = new CartResponse(
                savedCart.getId(),
                savedCart.getStatus(),
                List.of(),
                BigDecimal.ZERO,
                savedCart.getCreatedAt(),
                savedCart.getUpdatedAt()
        );
        //CartResponse response = cartMapper.toResponse(savedCart);
        log.info("Created Cart : {}", objectMapper.writeValueAsString(response));
        return response;
    }


//    @Transactional(readOnly = true)
//    public CartResponse getById(UUID cartId) {
//
//        // 1. Fetch Cart from PostgreSQL
//        Cart cart = cartRepository.findById(cartId)
//                .orElseThrow(() -> {
//                    log.warn("Cart not found with ID: {}", cartId);
//                    return new ResponseStatusException(
//                            HttpStatus.NOT_FOUND,
//                            "Cart not found with ID: " + cartId
//                    );
//                });
//
//        // 2. Convert Cart entity to DTO
//        CartResponse response = cartMapper.toResponse(cart);
//        log.info("Fetched Cart : {}", objectMapper.writeValueAsString(response));
//        return response;
//    }


    @Transactional(readOnly = true)
    public CartResponse getById(UUID cartId) {

        // 1. Find Cart
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> {
                    log.warn("Cart not found with ID: {}", cartId);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Cart not found with ID: " + cartId
                    );
                });

        // 2. Fetch all CartItems with their ProductVariants
        List<CartItem> cartItems = cartItemRepository
                .findByCart_IdOrderByCreatedAtAscIdAsc(cartId);

        // 3. Calculate each CartItem's line total
        List<CartItemResponse> itemResponses = new ArrayList<>();

        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem item : cartItems) {

            BigDecimal unitPrice = item.getProductVariant()
                    .getPrice();

            BigDecimal lineTotal = unitPrice.multiply(
                    BigDecimal.valueOf(item.getQuantity())
            );

            CartItemResponse itemResponse = cartItemMapper.toResponse(
                    item,
                    unitPrice,
                    lineTotal
            );

            itemResponses.add(itemResponse);

            subtotal = subtotal.add(lineTotal);
        }

        // 4. Return complete Cart response
        return new CartResponse(
                cart.getId(),
                cart.getStatus(),
                itemResponses,
                subtotal,
                cart.getCreatedAt(),
                cart.getUpdatedAt()
        );
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
                    log.warn(
                            "ProductVariant not found with ID: {}",
                            request.productVariantId()
                    );
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "ProductVariant not found with ID: "
                                    + request.productVariantId()
                    );
                });

        log.info(
                "Adding productVariant : {}",
                objectMapper.writeValueAsString(
                        variantMapper.toResponse(variant)
                )
        );

        // 4. Validate availability
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

        // 5. Check if this variant is already in the Cart
        Optional<CartItem> existingItem = cartItemRepository
                .findByCart_IdAndProductVariant_Id(
                        cartId,
                        request.productVariantId()
                );

        CartItem savedItem;

        if (existingItem.isPresent()) {

            // CASE 1: Existing CartItem - increase quantity
            log.info("ProductVariant already exists in Cart. Increasing quantity");

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

            item.setQuantity(newQuantity);

            // No cart.addItem(item) here.
            // The CartItem already belongs to this Cart.
            savedItem = cartItemRepository.saveAndFlush(item);

        } else {

            // CASE 2: New CartItem
            log.info("ProductVariant not found in Cart. Creating new CartItem");

            CartItem item = new CartItem();

            item.setProductVariant(variant);
            item.setQuantity(request.quantity());

            // Synchronize both sides of the relationship.
            // Internally:
            //   cart.getItems().add(item);
            //   item.setCart(cart);
            cart.addItem(item);

            savedItem = cartItemRepository.saveAndFlush(item);
        }

        // 6. Calculate prices
        BigDecimal unitPrice = variant.getPrice();

        BigDecimal lineTotal = unitPrice.multiply(
                BigDecimal.valueOf(savedItem.getQuantity())
        );

        // 7. Map response
        CartItemResponse response = cartItemMapper.toResponse(
                savedItem,
                unitPrice,
                lineTotal
        );

        log.info("Created/updated CartItem for cartId {} : {}", cartId, response);

        return response;
    }



    @Transactional
    public CartItemResponse updateItemQuantity(
            UUID cartId,
            UUID itemId,
            UpdateCartItemRequest request
    ) {

        // 1. Validate Cart
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
            log.warn("Cannot update items in a non-active cart");
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot update items in a non-active cart"
            );
        }

        // 3. Find CartItem
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("CartItem not found with ID: {}", itemId);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "CartItem not found with ID: " + itemId
                    );
                });

        // 4. Verify that the CartItem belongs to this Cart
        if (!item.getCart().getId().equals(cartId)) {
            log.warn("CartItem not found in the specified Cart");
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "CartItem not found in the specified Cart"
            );
        }

        // 5. Set the exact quantity
        item.setQuantity(request.quantity());

        // 6. Persist the change
        CartItem savedItem = cartItemRepository.saveAndFlush(item);

        // 7. Calculate the updated line total
        BigDecimal unitPrice = savedItem
                .getProductVariant()
                .getPrice();

        BigDecimal lineTotal = unitPrice.multiply(
                BigDecimal.valueOf(savedItem.getQuantity())
        );

        // 8. Convert entity to response DTO
        CartItemResponse response = cartItemMapper.toResponse(
                savedItem,
                unitPrice,
                lineTotal
        );
        log.info("Updated cartItem quantity for cartId {}, itemId {} : {}", cartId, itemId, response);
        return response;
    }


    @Transactional
    public void deleteItem(UUID cartId, UUID itemId) {

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
            log.warn("Cannot delete items from a non-active cart");
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete items from a non-active cart"
            );
        }

        // 3. Find the CartItem
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("CartItem not found with ID: {}", itemId);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "CartItem not found with ID: " + itemId
                    );
                });

        // 4. Verify that the item belongs to the Cart
        if (!item.getCart().getId().equals(cartId)) {
            log.warn("CartItem not found in the specified Cart");
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "CartItem not found in the specified Cart"
            );
        }

        // 5. Delete the CartItem
        cartItemRepository.delete(item);

        // 6. Log deletion
        log.info("Deleted CartItem. Cart ID: {}, Item ID: {}", cartId, itemId);
    }


    @Transactional
    public void clearCart(UUID cartId) {

        // 1. Find Cart
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> {
                    log.warn("Cart not found with ID: {}", cartId);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Cart not found with ID: " + cartId
                    );
                });

        // 2. Only ACTIVE carts can be cleared
        if (cart.getStatus() != CartStatus.ACTIVE) {
            log.warn("Cannot clear a non-active cart");
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot clear a non-active cart"
            );
        }

        // 3. Load the CartItems collection
        //    and remove all items using orphanRemoval
        cart.clearItems();

        //cart.setItems(null);

        // 4. Flush pending DELETE operations
        cartRepository.flush();

        log.info("Cleared all CartItems from Cart: {}", cartId);
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

