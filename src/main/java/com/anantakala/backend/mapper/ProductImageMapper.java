package com.anantakala.backend.mapper;

import com.anantakala.backend.dto.productimage.ProductImageRequest;
import com.anantakala.backend.dto.productimage.ProductImageResponse;
import com.anantakala.backend.entity.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductImageMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(
            target = "imageType",
            expression = "java(request.imageType() == null ? ImageType.GALLERY : request.imageType())"
    )
    @Mapping(
            target = "sortOrder",
            expression = "java(request.sortOrder() == null ? 0 : request.sortOrder())"
    )
    @Mapping(
            target = "primary",
            expression = "java(request.primary() != null && request.primary())"
    )
    ProductImage toEntity(ProductImageRequest request);

    @Mapping(target = "productId", source = "product.id")
    ProductImageResponse toResponse(ProductImage image);
}
