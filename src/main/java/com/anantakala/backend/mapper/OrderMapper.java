package com.anantakala.backend.mapper;

import com.anantakala.backend.dto.order.OrderResponse;
import com.anantakala.backend.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = OrderItemMapper.class
)
public interface OrderMapper {

    @Mapping(target = "cartId", source = "cart.id")
    OrderResponse toResponse(Order order);
}

//What does uses = OrderItemMapper.class do?
//Our Order entity contains:
//private List<OrderItem> items;
//
//
//Our OrderResponse contains:
//List<OrderItemResponse> items;
//
//
//MapStruct needs to convert each OrderItem into an OrderItemResponse.
//Order entity
//OrderMapper.toResponse(order)
//
//
//OrderItem 1
//OrderItemMapperItemResponse 1
//
//
//OrderItem 2
//OrderItemMapperItemResponse 2
//
//
//OrderItem 3
//OrderItemMapperItemResponse 3
//
//
//
//
//OrderResponse
//List<OrderItemResponse>
//
//
//MapStruct automatically generates the collection mapping code, conceptually similar to:
//List<OrderItemResponse> itemResponses = new ArrayList<>();
//
//for (OrderItem item : order.getItems()) {
//
//    itemResponses.add(
//            orderItemMapper.toResponse(item)
//    );
//}
//
//
//We don't need to manually write this loop for response mapping.
