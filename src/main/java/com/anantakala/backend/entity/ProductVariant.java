package com.anantakala.backend.entity;

import com.anantakala.backend.enums.ProductType;
import com.anantakala.backend.util.UuidV7Generator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "product_variants", schema = "public")
@Getter
@Setter
@NoArgsConstructor
public class ProductVariant {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "product_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_product_variants_product")
    )
    private Product product;

    @Column(name = "sku", nullable = false, unique = true, length = 80)
    private String sku;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 30)
    private ProductType productType;

    @Column(name = "size_label", length = 30)
    private String sizeLabel;

    @Column(name = "frame_finish", length = 30)
    private String frameFinish;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "currency",
            nullable = false,
            length = 3,
            columnDefinition = "char(3)")
    private String currency = "INR";

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UuidV7Generator.generate();
        }

        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}
