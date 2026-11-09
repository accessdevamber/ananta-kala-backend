
package com.anantakala.backend.dto.productvariant;

import com.anantakala.backend.enums.ProductType;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductVariantRequest(
        UUID productId,
        String sku,
        ProductType productType,
        String sizeLabel,
        String frameFinish,
        BigDecimal price,
        String currency,
        Boolean active
) {
}

//A few points:
//- productId is a UUID from the existing products table.
//- price is BigDecimal, not double, to avoid floating-point precision issues for monetary values.
//- active is Boolean, not primitive boolean, so we can distinguish an omitted value (null) from an explicit false.
//- currency defaults to INR if omitted.
