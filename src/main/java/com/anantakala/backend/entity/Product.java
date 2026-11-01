package com.anantakala.backend.entity;

import com.anantakala.backend.enums.ProductStatus;
import com.anantakala.backend.util.UuidV7Generator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "products", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@Slf4j
public class Product {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "category_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_products_category")
    )
    private Category category;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 220)
    private String slug;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProductStatus status = ProductStatus.DRAFT;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
            name = "specifications",
            nullable = false,
            columnDefinition = "jsonb"
    )
    private Map<String, Object> specifications = new HashMap<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UuidV7Generator.generate();
        }

        log.info("UUIDV7 id generated : {}", id);

        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        log.info("Inside preUpdate()");
        updatedAt = Instant.now();
    }
}

//SELECT
//    name,
//    specifications ->> 'orientation' AS orientation,
//    specifications -> 'dominantColors' AS dominant_colors
//FROM public.products;
//
//
//Here, ->> extracts a JSON value as text, while -> keeps it as JSONB.


//5. JSONB — default value and CHECK constraint
//Our SQL:
//specifications JSONB NOT NULL DEFAULT '{}'::jsonb
//This has four parts:
//Part	                    Meaning
//specifications	        Column name
//JSONB	PostgreSQL          binary JSON datatype
//NOT NULL	                SQL NULL is not allowed
//DEFAULT '{}'::jsonb	    Use an empty JSON object when the column is omitted

//What does '{}'::jsonb mean?

//'{}' represents an empty JSON object.
//::jsonb is PostgreSQL's type-cast syntax.
//Equivalent:
//CAST('{}' AS JSONB)
//Example:
//SELECT '{}'::jsonb;
//Result:
//{}
//This is different from an empty JSON array:
//SELECT '[]'::jsonb;
//Result:
//[]
//Why do we have a CHECK constraint?
//CONSTRAINT chk_products_specs_object
//    CHECK (jsonb_typeof(specifications) = 'object')

//Although JSONB accepts valid JSON, JSON itself can contain objects, arrays, strings, numbers, booleans and null.
//For example, these are all valid JSONB values:
//'{"ram":"8GB"}'::jsonb
//'["Java","SQL"]'::jsonb
//'"hello"'::jsonb
//'123'::jsonb
//'true'::jsonb
//But our product specifications should always be a JSON object containing key-value pairs.
//jsonb_typeof() checks the top-level JSON type.
//JSONB value	jsonb_typeof	Allowed?
//{"ram":"8GB"}	object	Yes
//{}	object	Yes
//["8GB","16GB"]	array	No
//"hello"	string	No
//123	number	No
//null (JSON null)	null	No
//This ensures that Product Service always receives a consistent top-level JSON structure.
//A Java mapping such as:
//@JdbcTypeCode(SqlTypes.JSON)
//@Column(
//        name = "specifications",
//        nullable = false,
//        columnDefinition = "jsonb"
//)
//private Map<String, Object> specifications = new HashMap<>();
//fits this design because a Map<String, Object> serializes naturally into a JSON object.
