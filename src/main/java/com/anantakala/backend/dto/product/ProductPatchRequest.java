package com.anantakala.backend.dto.product;

import tools.jackson.databind.JsonNode;

public record ProductPatchRequest(
        JsonNode categoryId,
        JsonNode name,
        JsonNode slug,
        JsonNode description,
        JsonNode status,
        JsonNode specifications
) {
}
