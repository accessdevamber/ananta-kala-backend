package com.anantakala.backend.entity;

import com.anantakala.backend.enums.OrderStatus;
import com.anantakala.backend.util.UuidV7Generator;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@Slf4j
public class Order {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(
            name = "order_number",
            nullable = false,
            unique = true,
            length = 50,
            updatable = false
    )
    private String orderNumber;


    //The Order entity is the owning side because the cart_id foreign key is present in the orders table.
    //We don't need to add this to Cart:
    //@OneToOne(mappedBy = "cart")
    //private Order order;
    //
    //
    //That would make the relationship bidirectional, but currently we don't need to navigate from Cart to Order.
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "cart_id",
            nullable = false,
            unique = true,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_orders_cart")
    )
    private Cart cart;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(
            name = "subtotal",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(
            name = "shipping_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal shippingAmount = BigDecimal.ZERO;

    @Column(
            name = "discount_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(
            name = "total_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(
            name = "currency",
            nullable = false,
            length = 3,
            columnDefinition = "CHAR(3)"
    )
    private String currency = "INR";

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.PERSIST
    )
    private List<OrderItem> items = new ArrayList<>();

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {

        if (id == null) {
            //id = UuidCreator.getTimeOrderedEpoch();
            id = UuidV7Generator.generate();
        }

        Instant now = Instant.now();

        createdAt = now;
        updatedAt = now;

        log.info("Persisting Order with ID: {}", id);
    }

    @PreUpdate
    public void preUpdate() {
        log.info("Inside preUpdate()");
        updatedAt = Instant.now();

        log.info("Updating Order with ID: {}", id);
    }

    public void addItem(OrderItem item) {

        items.add(item);
        item.setOrder(this);
    }
}
