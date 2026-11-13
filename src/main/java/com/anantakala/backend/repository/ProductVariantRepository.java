
package com.anantakala.backend.repository;

import com.anantakala.backend.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

    Optional<ProductVariant> findBySku(String sku);

    boolean existsBySku(String sku);


    //This works because Spring Data JPA can navigate the relationship:
    //ProductVariant.product.id
    //List<ProductVariant> findByProductId(UUID productId);
    //The underscore explicitly separates the relationship product from its property id.
    //
    //Both work for your current entity structure.
    List<ProductVariant> findByProduct_Id(UUID productId);
}
