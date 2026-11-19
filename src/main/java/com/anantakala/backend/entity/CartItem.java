package com.anantakala.backend.entity;

import com.anantakala.backend.util.UuidV7Generator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cart_items", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@Slf4j
public class CartItem {

    @Id
    @Column(
            name = "id",
            nullable = false,
            updatable = false
    )
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "cart_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_cart_items_cart"
            )
    )
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "product_variant_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_cart_items_variant"
            )
    )
    private ProductVariant productVariant;

    @Column(
            name = "quantity",
            nullable = false
    )
    private int quantity = 1;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {

        if (id == null) {
            id = UuidV7Generator.generate();
        }

        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}

