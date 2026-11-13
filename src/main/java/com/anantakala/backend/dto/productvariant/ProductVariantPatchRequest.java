package com.anantakala.backend.dto.productvariant;

import tools.jackson.databind.JsonNode;

public record ProductVariantPatchRequest(
        JsonNode productId,
        JsonNode sku,
        JsonNode productType,
        JsonNode sizeLabel,
        JsonNode frameFinish,
        JsonNode price,
        JsonNode currency,
        JsonNode active
) {
}
