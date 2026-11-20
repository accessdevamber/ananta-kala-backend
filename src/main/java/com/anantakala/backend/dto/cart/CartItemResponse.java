package com.anantakala.backend.dto.cart;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CartItemResponse(
        UUID id,
        UUID cartId,
        UUID productVariantId,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal,
        Instant createdAt,
        Instant updatedAt
) {
}

//Remember:
//- unitPrice comes from ProductVariant.price.
//- lineTotal = unitPrice × quantity.
//- Neither value is stored in the cart_items table.
