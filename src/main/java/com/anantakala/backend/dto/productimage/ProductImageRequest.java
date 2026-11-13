package com.anantakala.backend.dto.productimage;

import com.anantakala.backend.enums.ImageType;

import java.util.UUID;

public record ProductImageRequest(
        UUID productId,
        String imageUrl,
        ImageType imageType,
        String altText,
        Integer sortOrder,
        Boolean primary
) {
}

//We use Integer and Boolean instead of primitive int and boolean so we can distinguish an omitted value
// from an explicitly supplied value.
//
//For POST, we'll use defaults:
//- imageType = GALLERY
//- sortOrder = 0
//- primary = false