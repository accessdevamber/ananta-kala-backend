package com.anantakala.backend.controller;

import com.anantakala.backend.dto.PageResponse;
import com.anantakala.backend.dto.order.CreateOrderRequest;
import com.anantakala.backend.dto.order.OrderResponse;
import com.anantakala.backend.service.OrderService;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.Set;
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



    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getById(
            @PathVariable UUID orderId
    ) {

        return ResponseEntity.ok(
                orderService.getById(orderId)
        );
    }

    @GetMapping
    public ResponseEntity<PageResponse<OrderResponse>> getAll(

            @RequestParam(defaultValue = "0")
            int page,
            @RequestParam(defaultValue = "10")
            int size,
            @RequestParam(defaultValue = "createdAt,desc")
            String sort
    ) {

        // Validate page number
        if (page < 0) {
            log.warn("Page number cannot be negative");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page number cannot be negative"
            );
        }

        // Validate page size
        if (size < 1 || size > 100) {
            log.warn("Page size must be between 1 and 100");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page size must be between 1 and 100"
            );
        }

        // Extract sort field and direction
        String[] sortParts = sort.split(",", -1);

        if (sortParts.length != 2) {
            log.warn("Sort must be in field,direction format");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sort must be in field,direction format"
            );
        }

        String sortField = sortParts[0].trim();
        String sortDirection = sortParts[1].trim();

        // Validate supported sorting fields
        Set<String> allowedSortFields = Set.of(
                "createdAt",
                "updatedAt",
                "totalAmount",
                "orderNumber",
                "status",
                "id"
        );

        if (!allowedSortFields.contains(sortField)) {
            log.warn("Unsupported sort field: {}", sortField);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported sort field: " + sortField
            );
        }

        // Convert String direction into Sort.Direction
        Sort.Direction direction = Sort.Direction
                .fromOptionalString(sortDirection)
                .orElseThrow(() -> {
                    log.warn("Sort direction must be asc or desc");
                    return new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Sort direction must be asc or desc"
                    );
                });

        // Construct Sort object
        Sort orderSort = Sort.by(direction, sortField);

        // Add unique tie-breaker for deterministic sorting
        if (!"id".equals(sortField)) {

            orderSort = orderSort.and(
                    Sort.by(direction, "id")
            );
        }

        // Construct Pageable object
        Pageable pageable = PageRequest.of(page, size, orderSort);

        return ResponseEntity.ok(orderService.getAll(pageable));
    }

    //demo
//    @GetMapping("/demo/n-plus-one")
//    public ResponseEntity<Void> demonstrateNPlusOne() {
//
//        orderService.demonstrateNPlusOne();
//
//        return ResponseEntity.ok().build();
//    }

    //demo pagination
//    @GetMapping("/demo/pagination")
//    public ResponseEntity<Void> demonstrateCollectionPagination() {
//
//        orderService.demonstrateCollectionPagination();
//
//        return ResponseEntity.ok().build();
//    }

}
