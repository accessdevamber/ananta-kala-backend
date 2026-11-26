package com.anantakala.backend.mapper;

import com.anantakala.backend.dto.order.OrderItemResponse;
import com.anantakala.backend.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(
            target = "productVariantId",
            source = "productVariant.id"
    )
    OrderItemResponse toResponse(OrderItem orderItem);
}
