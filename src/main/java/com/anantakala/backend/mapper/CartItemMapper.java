package com.anantakala.backend.mapper;


import com.anantakala.backend.dto.cart.CartItemResponse;
import com.anantakala.backend.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(target = "cartId", source = "cartItem.cart.id")
    @Mapping(
            target = "productVariantId",
            source = "cartItem.productVariant.id"
    )
    @Mapping(target = "unitPrice", source = "unitPrice")
    @Mapping(target = "lineTotal", source = "lineTotal")
    CartItemResponse toResponse(
            CartItem cartItem,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    );
}

