package com.anantakala.backend.dto.cart;

import com.anantakala.backend.enums.CartStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CartResponse(
        UUID id,
        CartStatus status,
        List<CartItemResponse> items,
        BigDecimal subtotal,
        Instant createdAt,
        Instant updatedAt
) {
}
