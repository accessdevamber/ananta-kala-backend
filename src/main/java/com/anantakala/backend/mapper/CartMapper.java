package com.anantakala.backend.mapper;

import com.anantakala.backend.dto.cart.CartResponse;
import com.anantakala.backend.entity.Cart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(target = "items", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    CartResponse toResponse(Cart cart);
}
