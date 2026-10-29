package com.anantakala.backend.dto.category;

import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String slug,
        String description,
        UUID parentId,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
