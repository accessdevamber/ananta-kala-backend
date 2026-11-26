package com.anantakala.backend.controller;

import com.anantakala.backend.dto.order.CreateOrderRequest;
import com.anantakala.backend.dto.order.OrderResponse;
import com.anantakala.backend.service.OrderService;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> create(
            @Valid @RequestBody CreateOrderRequest request
    ) {

        OrderResponse response = orderService.create(request);

        return ResponseEntity
                .created(URI.create("/api/orders/" + response.id()))
                .body(response);
    }

    //demo
//    @GetMapping("/demo/n-plus-one")
//    public ResponseEntity<Void> demonstrateNPlusOne() {
//
//        orderService.demonstrateNPlusOne();
//
//        return ResponseEntity.ok().build();
//    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getById(
            @PathVariable UUID orderId
    ) {

        return ResponseEntity.ok(
                orderService.getById(orderId)
        );
    }

}
