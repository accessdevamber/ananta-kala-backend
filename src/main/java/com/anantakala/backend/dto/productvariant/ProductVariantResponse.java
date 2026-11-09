
package com.anantakala.backend.dto.productvariant;

import com.anantakala.backend.enums.ProductType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductVariantResponse(
        UUID id,
        UUID productId,
        String sku,
        ProductType productType,
        String sizeLabel,
        String frameFinish,
        BigDecimal price,
        String currency,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
