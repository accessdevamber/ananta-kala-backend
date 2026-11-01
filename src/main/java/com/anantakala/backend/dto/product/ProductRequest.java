
package com.anantakala.backend.dto.product;

import com.anantakala.backend.enums.ProductStatus;

import java.util.Map;
import java.util.UUID;

public record ProductRequest(
        UUID categoryId,
        String name,
        String slug,
        String description,
        ProductStatus status,
        Map<String, Object> specifications
) {
}
