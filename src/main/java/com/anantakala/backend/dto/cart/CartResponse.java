package com.anantakala.backend.dto.cart;

import com.anantakala.backend.enums.CartStatus;

import java.time.Instant;
import java.util.UUID;

public record CartResponse(
        UUID id,
        CartStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
