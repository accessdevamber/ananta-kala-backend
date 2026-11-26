package com.anantakala.backend.dto.order;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateOrderRequest(

        @NotNull(message = "Cart ID is required")
        UUID cartId
) {
}
