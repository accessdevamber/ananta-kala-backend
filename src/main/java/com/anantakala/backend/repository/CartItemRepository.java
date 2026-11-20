package com.anantakala.backend.repository;

import com.anantakala.backend.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    Optional<CartItem> findByCart_IdAndProductVariant_Id(
            UUID cartId,
            UUID productVariantId
    );
}

//Spring Data JPA derives the query from the method name.
//
//Conceptually:
//SELECT *
//FROM public.cart_items
//WHERE cart_id = ?
//  AND product_variant_id = ?;
//
//
//This method helps us determine whether to insert a new CartItem or increment an existing one.

