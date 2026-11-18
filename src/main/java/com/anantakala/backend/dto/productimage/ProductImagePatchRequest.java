package com.anantakala.backend.dto.productimage;

import tools.jackson.databind.JsonNode;

public record ProductImagePatchRequest(
        JsonNode productId,
        JsonNode imageUrl,
        JsonNode imageType,
        JsonNode altText,
        JsonNode sortOrder,
        JsonNode primary
) {
}

