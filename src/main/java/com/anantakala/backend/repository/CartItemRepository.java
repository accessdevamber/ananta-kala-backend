package com.anantakala.backend.repository;

import com.anantakala.backend.entity.CartItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    Optional<CartItem> findByCart_IdAndProductVariant_Id(
            UUID cartId,
            UUID productVariantId
    );

    @EntityGraph(attributePaths = "productVariant")
    List<CartItem> findByCart_IdOrderByCreatedAtAscIdAsc(
            UUID cartId
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


//Why @EntityGraph?
//Remember that CartItem has:
//@ManyToOne(fetch = FetchType.LAZY)
//private ProductVariant productVariant;
//
//
//By default, Hibernate may load ProductVariant separately when we access its price.
//
//With:
//@EntityGraph(attributePaths = "productVariant")
//
//
//we instruct JPA to fetch the related ProductVariants as part of the CartItem query.
//
//This helps avoid the N+1 query problem.
//
//We only fetch ProductVariant here because we need its price.

