package com.anantakala.backend.mapper;

import com.anantakala.backend.dto.cart.CartResponse;
import com.anantakala.backend.entity.Cart;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CartMapper {

    CartResponse toResponse(Cart cart);
}
