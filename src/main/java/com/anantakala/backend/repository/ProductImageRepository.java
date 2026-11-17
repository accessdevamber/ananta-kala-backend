package com.anantakala.backend.repository;

import com.anantakala.backend.entity.ProductImage;
import com.anantakala.backend.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {

    //List<ProductImage> findByProductIdOrderBySortOrderAsc(UUID productId);
    List<ProductImage> findByProduct_IdOrderBySortOrderAscIdAsc(UUID productId);

    boolean existsByProductIdAndPrimaryTrue(UUID productId);
    //List<ProductImage> findByProduct_Id(UUID productId);
}

//CREATE UNIQUE INDEX uk_product_images_primary
//    ON public.product_images(product_id)
//    WHERE is_primary = TRUE;
//The index guarantees at most one primary image per Product, not exactly one.
//A Product with no primary image is still allowed.
