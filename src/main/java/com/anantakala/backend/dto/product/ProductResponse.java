
package com.anantakala.backend.dto.product;

import com.anantakala.backend.enums.ProductStatus;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        UUID categoryId,
        String name,
        String slug,
        String description,
        ProductStatus status,
        Map<String, Object> specifications,
        Instant createdAt,
        Instant updatedAt
) {
}
