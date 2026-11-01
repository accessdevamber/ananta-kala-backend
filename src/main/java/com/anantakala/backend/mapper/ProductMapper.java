
package com.anantakala.backend.mapper;

import com.anantakala.backend.dto.product.ProductRequest;
import com.anantakala.backend.dto.product.ProductResponse;
import com.anantakala.backend.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(
            target = "status",
            expression = "java(request.status() == null ? com.anantakala.backend.enums.ProductStatus.DRAFT : request.status())"
    )
    @Mapping(
            target = "specifications",
            expression = "java(request.specifications() == null ? new java.util.HashMap<>() : request.specifications())"
    )
    Product toEntity(ProductRequest request);

    @Mapping(target = "categoryId", source = "category.id")
    ProductResponse toResponse(Product product);
}
