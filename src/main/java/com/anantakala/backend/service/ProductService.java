
package com.anantakala.backend.service;

import com.anantakala.backend.dto.product.ProductPatchRequest;
import com.anantakala.backend.dto.product.ProductRequest;
import com.anantakala.backend.dto.product.ProductResponse;
import com.anantakala.backend.entity.Category;
import com.anantakala.backend.entity.Product;
import com.anantakala.backend.enums.ProductStatus;
import com.anantakala.backend.mapper.ProductMapper;
import com.anantakala.backend.repository.CategoryRepository;
import com.anantakala.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final ObjectMapper objectMapper;
    private final JsonMapper jsonMapper;

    @Transactional
    public ProductResponse create(ProductRequest request) {

        if (request.categoryId() == null) {
            log.warn("product's category Id is NULL or blank");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "categoryId is required"
            );
        }

        if (request.name() == null || request.name().isBlank()) {
            log.warn("product name is NULL or blank");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product name is required"
            );
        }

        if (request.slug() == null || request.slug().isBlank()) {
            log.warn("product slug is NULL or blank");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product slug is required"
            );
        }

        if (productRepository.existsBySlug(request.slug())) {
            log.warn("product slug already exists");
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product slug already exists"
            );
        }

        Category category = categoryRepository
                .findById(request.categoryId())
                .orElseThrow(() -> {
                    log.warn("Product's category not found");
                    throw new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Product's category not found"
                    );
        });

        Product product = productMapper.toEntity(request);

        product.setCategory(category);

        Product saved = productRepository.save(product);

        ProductResponse response = productMapper.toResponse(saved);
        log.info("Saved product : {}", objectMapper.writeValueAsString(response));
        return response;
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(UUID id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found");
                    throw new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Product not found with ID: " + id
                    );
                });
        ProductResponse response = productMapper.toResponse(product);
        log.info("Fetched product by Id : {}", objectMapper.writeValueAsString(response));
        return response;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {

        List<ProductResponse> response = productRepository.findAll()
                .stream()
                .map(productMapper::toResponse)
                .toList();
        log.info("Fetched all product : {}", objectMapper.writeValueAsString(response));
        return response;
    }

    @Transactional(readOnly = true)
    public ProductResponse findBySlug(String slug) {

        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> {
                    log.warn("Product not found");
                    throw new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Product not found with slug: " + slug
                    );
                });

        ProductResponse response = productMapper.toResponse(product);
        log.info("Fetched product by slug : {}", objectMapper.writeValueAsString(response));
        return response;
    }


    @Transactional
    public ProductResponse update(UUID id, ProductRequest request) {

        // 1. Fetch existing product
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found");
                    throw new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Product not found with ID: " + id
                    );
                });

        // 2. Validate required fields
        if (request.categoryId() == null) {
            log.warn("Product categoryId missing");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "categoryId is required"
            );
        }

        if (request.name() == null || request.name().isBlank()) {
            log.warn("Product name missing");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product name is required"
            );
        }

        if (request.slug() == null || request.slug().isBlank()) {
            log.warn("Product slug missing");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product slug is required"
            );
        }

        if (request.status() == null) {
            log.warn("Product status missing");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product status is required"
            );
        }

        if (request.specifications() == null) {
            log.warn("Product Specifications missing");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Specifications are required (use {} if empty)"
            );
        }

        // 3. Check slug uniqueness, excluding current product
        productRepository.findBySlug(request.slug())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Product slug already exists"
                        );
                    }
                });

        // 4. Resolve Category foreign key
        Category category = categoryRepository
                .findById(request.categoryId())
                .orElseThrow(() -> {
                    log.warn("Product Category missing");
                    throw new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Category not found"
                    );
                });

        // 5. Update editable fields
        product.setCategory(category);
        product.setName(request.name());
        product.setSlug(request.slug());
        product.setDescription(request.description());
        product.setStatus(request.status());
        product.setSpecifications(new HashMap<>(request.specifications()));

        // 6. Convert updated entity to response
        //
        //flush -> update query runs and updates the DB along with @PreUpdate run which sets the updatedAt field.
        //Then the response is created with all the updated fields including updatedAt and prints and returns
        productRepository.flush();
        ProductResponse response = productMapper.toResponse(product);
        log.info("Updated product by Id : {}", objectMapper.writeValueAsString(response));
        return response;
    }


    @Transactional
    public ProductResponse patch(UUID id, ProductPatchRequest request) {

        // 1. Fetch existing product
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found with ID: {}", id);
                    throw new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Product not found with ID: " + id
                    );
                });

        // 2. Update name
        if (request.name() != null) {
            product.setName(requiredJsonText(request.name(), "name"));
        }

        // 3. Update slug
        if (request.slug() != null) {
            String slug = requiredJsonText(request.slug(), "slug");

            productRepository.findBySlug(slug)
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            log.warn("Product slug already exists");
                            throw new ResponseStatusException(
                                    HttpStatus.CONFLICT,
                                    "Product slug already exists"
                            );
                        }
                    });

            product.setSlug(slug);
        }

        // 4. Update description (nullable)
        if (request.description() != null) {
            JsonNode node = request.description();

            if (node.isNull()) {
                product.setDescription(null);
            } else if (node.isTextual()) {
                product.setDescription(node.asText());
            } else {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "description must be a string or null"
                );
            }
        }

        // 5. Update status
        if (request.status() != null) {
            String value = requiredJsonText(request.status(), "status");

            try {
                product.setStatus(ProductStatus.valueOf(value));
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid product status: " + value
                );
            }
        }

        // 6. Update Category relationship
        if (request.categoryId() != null) {
            UUID categoryId = requiredJsonUuid(
                    request.categoryId(), "categoryId"
            );

            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> {
                        log.warn("Category not found");
                         return new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Category not found"
                        );
                    });

            product.setCategory(category);
        }

        // 7. Merge JSONB specifications
        if (request.specifications() != null) {
            JsonNode node = request.specifications();

            if (!node.isObject()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "specifications must be a JSON object"
                );
            }

            Map<String, Object> updated = new HashMap<>(
                    product.getSpecifications()
            );

            node.properties().forEach(entry -> {
                String key = entry.getKey();
                JsonNode value = entry.getValue();

                if (value.isNull()) {
                    updated.remove(key);
                } else {
                    //The code below uses jsonMapper.convertValue() to convert individual Jackson JsonNode
                    //values into Java objects suitable for our Map<String, Object>.
                    updated.put(key, jsonMapper.convertValue(
                            value, Object.class
                    ));
                }
            });

            product.setSpecifications(updated);
        }

        // 8. Flush changes and return response
        productRepository.flush();

        ProductResponse response = productMapper.toResponse(product);
        log.info("Updated product using patch : {}", objectMapper.writeValueAsString(response));
        return response;
    }

    private String requiredJsonText(JsonNode node, String field) {
        if (!node.isTextual() || node.asText().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    field + " must be a non-blank string"
            );
        }

        return node.asText();
    }

    private UUID requiredJsonUuid(JsonNode node, String field) {
        if (!node.isTextual()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    field + " must be a UUID string"
            );
        }

        try {
            return UUID.fromString(node.asText());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid UUID for " + field
            );
        }
    }

}


//Why don't we call productRepository.save(product)?
//Notice that our code ends with:
//return productMapper.toResponse(product);
//
//
//Unlike our earlier Category PUT example, we're not explicitly calling save().
//
//That's intentional.
//
//We fetched the Product using:
//Product product = productRepository.findById(id).orElseThrow();
//
//
//Because the method is annotated with:
//@Transactional
//
//
//the returned Product is a managed JPA entity.
//
//When we change its fields:
//product.setName(request.name());
//product.setStatus(request.status());
//
//
//Hibernate tracks those changes through dirty checking.
//
//At transaction commit, Hibernate detects the changes and executes an SQL UPDATE.
//
//Conceptually:
//UPDATE public.products
//SET
//    category_id = ?,
//    name = ?,
//    slug = ?,
//    description = ?,
//    status = ?,
//    specifications = ?,
//    updated_at = ?
//WHERE id = ?;
//
//
//Your existing @PreUpdate method updates updatedAt when Hibernate performs an entity update.
//
//One detail: the response is mapped before transaction commit.
// If you want the response to contain the newly generated updatedAt value, explicitly flush before mapping:
//productRepository.flush();
//return productMapper.toResponse(product);
//
//
//This forces pending changes to be synchronized with PostgreSQL before the response is constructed.
// The transaction still commits afterward.
//
//For our implementation, use these two lines at the end of update():
//productRepository.flush();
//return productMapper.toResponse(product);