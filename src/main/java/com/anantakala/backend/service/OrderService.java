package com.anantakala.backend.service;

import com.anantakala.backend.dto.order.CreateOrderRequest;
import com.anantakala.backend.dto.order.OrderResponse;
import com.anantakala.backend.entity.*;
import com.anantakala.backend.enums.CartStatus;
import com.anantakala.backend.enums.OrderStatus;
import com.anantakala.backend.enums.ProductStatus;
import com.anantakala.backend.mapper.OrderMapper;
import com.anantakala.backend.repository.CartItemRepository;
import com.anantakala.backend.repository.CartRepository;
import com.anantakala.backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {

        UUID cartId = request.cartId();

        log.info("Creating Order for Cart ID: {}", cartId);

        // 1. Fetch and lock Cart
        Cart cart = cartRepository.findByIdForUpdate(cartId)
                .orElseThrow(() -> {
                    log.warn("Cart not found: {}", cartId);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Cart not found: " + cartId
                    );
                });

        // 2. Validate Cart status
        if (cart.getStatus() != CartStatus.ACTIVE) {
            log.warn("Cart is not ACTIVE");
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cart is not ACTIVE"
            );
        }

        // 3. Check if Order already exists
        if (orderRepository.existsByCart_Id(cartId)) {
            log.warn("Order already exists for this Cart");
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Order already exists for this Cart"
            );
        }

        // 4. Fetch CartItems
        List<CartItem> cartItems = cartItemRepository.findByCart_IdOrderByCreatedAtAscIdAsc(cartId);

        // 5. Validate Cart is not empty
        if (cartItems.isEmpty()) {
            log.warn("Cannot create Order from an empty Cart");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot create Order from an empty Cart"
            );
        }

        // 6. Create Order
        Order order = new Order();

        order.setCart(cart);
        order.setOrderNumber(generateOrderNumber());
        order.setStatus(OrderStatus.PENDING);
        order.setCurrency("INR");

        BigDecimal subtotal = BigDecimal.ZERO;

        // 7. Convert CartItems into OrderItems
        for (CartItem cartItem : cartItems) {

            ProductVariant variant = cartItem.getProductVariant();

            Product product = variant.getProduct();

            // Validate ProductVariant
            if (!variant.isActive()) {
                log.warn("ProductVariant is inactive: {}", variant.getId());
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "ProductVariant is inactive: "
                                + variant.getId()
                );
            }

            // Validate Product
            if (product.getStatus() != ProductStatus.ACTIVE) {
                log.warn("Product is inactive: {}", product.getId());
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Product is not ACTIVE: "
                                + product.getId()
                );
            }

            // Validate currency
            if (!"INR".equals(variant.getCurrency())) {
                log.warn("Unsupported currency: {}", variant.getCurrency());
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Unsupported currency: "
                                + variant.getCurrency()
                );
            }

            // Calculate line total
            BigDecimal unitPrice = variant.getPrice();

            BigDecimal lineTotal = unitPrice.multiply(
                    BigDecimal.valueOf(cartItem.getQuantity())
            );

            // Create OrderItem
            OrderItem orderItem = new OrderItem();

            orderItem.setProductVariant(variant);

            // Product snapshot
            orderItem.setProductName(product.getName());
            orderItem.setSku(variant.getSku());
            orderItem.setProductType(variant.getProductType());
            orderItem.setSizeLabel(variant.getSizeLabel());
            orderItem.setFrameFinish(variant.getFrameFinish());

            // Quantity and price snapshot
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(unitPrice);
            orderItem.setLineTotal(lineTotal);

            // Synchronize both sides
            order.addItem(orderItem);

            // Add line total to subtotal
            subtotal = subtotal.add(lineTotal);
        }

        // 8. Calculate final Order amount
        BigDecimal shippingAmount = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;

        BigDecimal totalAmount = subtotal
                .add(shippingAmount)
                .subtract(discountAmount);

        order.setSubtotal(subtotal);
        order.setShippingAmount(shippingAmount);
        order.setDiscountAmount(discountAmount);
        order.setTotalAmount(totalAmount);

        // 9. Persist Order and OrderItems
        Order savedOrder = orderRepository.saveAndFlush(order);

        // 10. Update Cart status
        cart.setStatus(CartStatus.CHECKED_OUT);

        cartRepository.flush();

        log.info(
                "Order created successfully. Order ID: {}, "
                        + "Order Number: {}, Total Amount: {}",
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                savedOrder.getTotalAmount()
        );

        // 11. Convert to response
        return orderMapper.toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(UUID orderId) {

        log.info("Fetching Order with ID: {}", orderId);

        //no optimisation
//        Order order = orderRepository.findById(orderId)
//                .orElseThrow(() -> new ResponseStatusException(
//                        HttpStatus.NOT_FOUND,
//                        "Order not found: " + orderId
//                ));

        //with @EntityGraph(attributePaths = "items")
//        Order order = orderRepository.findByIdWithItemsUsingEntityGraph(orderId)
//                .orElseThrow(() -> new ResponseStatusException(
//                        HttpStatus.NOT_FOUND,
//                        "Order not found: " + orderId
//                ));

        //with @Query("""
        //        SELECT o
        //        FROM Order o
        //        LEFT JOIN FETCH o.items
        //        WHERE o.id = :orderId
        //        """)
        Order order = orderRepository.findByIdWithItemsUsingJoinFetch(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found: " + orderId
                ));

        OrderResponse response = orderMapper.toResponse(order);

        log.info(
                "Order fetched successfully. Order ID: {}, "
                        + "Order Number: {}, Total Amount: {}",
                response.id(),
                response.orderNumber(),
                response.totalAmount()
        );

        return response;
    }

    //demo
//    @Transactional(readOnly = true)
//    public void demonstrateNPlusOne() {
//
//        //List<Order> orders = orderRepository.findAll();
//        List<Order> orders = orderRepository.findAllWithItems();
//
//        log.info("Total Orders: {}", orders.size());
//
//        for (Order order : orders) {
//
//            log.info(
//                    "Order ID: {}, Number of items: {}",
//                    order.getId(),
//                    order.getItems().size()
//            );
//        }
//    }

    private String generateOrderNumber() {

        LocalDate now = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        log.info("LocalDate now : {}", now);
        String date = now.format(DateTimeFormatter.BASIC_ISO_DATE);

        return "AK-" + date + "-" + UUID.randomUUID();
    }
}
