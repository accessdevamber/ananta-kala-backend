package com.anantakala.backend.dto.category;

import java.util.UUID;

//only all params constructor present in record
//use categoryRequest.name(), categoryRequest.slug() etc for getters. No "get" keyword here.
//no setter
public record CategoryRequest(
        String name,
        String slug,
        String description,
        UUID parentId,
        Boolean active
) {
}
