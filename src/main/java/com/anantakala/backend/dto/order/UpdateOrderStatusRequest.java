package com.anantakala.backend.dto.order;

import com.anantakala.backend.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(

        @NotNull(message = "Order status is required")
        OrderStatus status
) {
}
