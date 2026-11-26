package com.anantakala.backend.entity;

import com.anantakala.backend.enums.ProductType;
import com.anantakala.backend.util.UuidV7Generator;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_items", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@Slf4j
public class OrderItem {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "order_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_order_items_order")
    )
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "product_variant_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_order_items_variant")
    )
    private ProductVariant productVariant;

    // Product snapshot
    @Column(
            name = "product_name",
            nullable = false,
            length = 200,
            updatable = false
    )
    private String productName;

    @Column(
            name = "sku",
            nullable = false,
            length = 80,
            updatable = false
    )
    private String sku;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "product_type",
            nullable = false,
            length = 30,
            updatable = false
    )
    private ProductType productType;

    @Column(
            name = "size_label",
            length = 50,
            updatable = false
    )
    private String sizeLabel;

    @Column(
            name = "frame_finish",
            length = 50,
            updatable = false
    )
    private String frameFinish;

    // Quantity snapshot
    @Column(
            name = "quantity",
            nullable = false,
            updatable = false
    )
    private int quantity;

    // Price snapshot
    @Column(
            name = "unit_price",
            nullable = false,
            precision = 12,
            scale = 2,
            updatable = false
    )
    private BigDecimal unitPrice;

    @Column(
            name = "line_total",
            nullable = false,
            precision = 12,
            scale = 2,
            updatable = false
    )
    private BigDecimal lineTotal;

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

        log.info("Persisting OrderItem with ID: {}", id);
    }

    @PreUpdate
    public void preUpdate() {

        updatedAt = Instant.now();

        log.info("Updating OrderItem with ID: {}", id);
    }
}

