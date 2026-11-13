
package com.anantakala.backend.mapper;

import com.anantakala.backend.dto.productvariant.ProductVariantRequest;
import com.anantakala.backend.dto.productvariant.ProductVariantResponse;
import com.anantakala.backend.entity.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductVariantMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(
            target = "currency",
            expression = "java(request.currency() == null ? \"INR\" : request.currency())"
    )
    @Mapping(
            target = "active",
            expression = "java(request.active() == null ? true : request.active())"
    )
    ProductVariant toEntity(ProductVariantRequest request);

    @Mapping(target = "productId", source = "product.id")
    ProductVariantResponse toResponse(ProductVariant variant);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(ProductVariantRequest request, @MappingTarget ProductVariant variant);
}

//Why ignore product?
//
//Because the request contains UUID productId, whereas the entity contains Product product.
//
//We'll resolve the Product using ProductRepository in our service.