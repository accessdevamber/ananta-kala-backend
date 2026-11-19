package com.anantakala.backend.entity;

import com.anantakala.backend.enums.CartStatus;
import com.anantakala.backend.util.UuidV7Generator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "carts", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@Slf4j
public class Cart {

    @Id
    @Column(
            name = "id",
            nullable = false,
            updatable = false
    )
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private CartStatus status = CartStatus.ACTIVE;

    @OneToMany(
            mappedBy = "cart",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<CartItem> items = new ArrayList<>();

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

        if (status == null) {
            status = CartStatus.ACTIVE;
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

    public void addItem(CartItem item) {
        items.add(item);
        item.setCart(this);
    }

    public void removeItem(CartItem item) {
        items.remove(item);
        item.setCart(null);
    }
}

//mappedBy = "cart"
//
//This means the cart field inside CartItem owns the relationship.
//
//The foreign key exists in the cart_items table, not in carts.
//
//cascade = CascadeType.ALL
//
//Operations on the Cart can propagate to its CartItems.
//
//For example, deleting a Cart through JPA can also delete its associated CartItems.
//
//orphanRemoval = true
//
//If a CartItem is removed from the Cart's items collection, Hibernate can delete that CartItem from the database.

