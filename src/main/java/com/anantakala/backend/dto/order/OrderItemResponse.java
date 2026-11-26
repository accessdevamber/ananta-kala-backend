package com.anantakala.backend.dto.order;

import com.anantakala.backend.enums.ProductType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderItemResponse(

        UUID id,
        UUID orderId,
        UUID productVariantId,
        String productName,
        String sku,
        ProductType productType,
        String sizeLabel,
        String frameFinish,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal,
        Instant createdAt,
        Instant updatedAt
) {
}
