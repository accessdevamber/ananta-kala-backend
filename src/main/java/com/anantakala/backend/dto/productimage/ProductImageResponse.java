package com.anantakala.backend.dto.productimage;

import com.anantakala.backend.enums.ImageType;

import java.time.Instant;
import java.util.UUID;

public record ProductImageResponse(
        UUID id,
        UUID productId,
        String imageUrl,
        ImageType imageType,
        String altText,
        int sortOrder,
        boolean primary,
        Instant createdAt,
        Instant updatedAt
) {
}
