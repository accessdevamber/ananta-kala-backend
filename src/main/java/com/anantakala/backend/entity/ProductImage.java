package com.anantakala.backend.entity;

import com.anantakala.backend.enums.ImageType;
import com.anantakala.backend.util.UuidV7Generator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "product_images", schema = "public")
@Getter
@Setter
@NoArgsConstructor
public class ProductImage {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "product_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_product_images_product")
    )
    private Product product;

    @Column(name = "image_url", nullable = false, columnDefinition = "text")
    private String imageUrl;

    //It identifies the purpose of the image
    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", nullable = false, length = 20)
    private ImageType imageType = ImageType.GALLERY;

    @Column(name = "alt_text", length = 255)
    private String altText;

    //sort_order — In which order should images appear?
    //This controls the sequence of images displayed in our React product gallery.
    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    //is_primary — Which image represents the product?
    //This is a boolean (TRUE or FALSE).
    //The primary image is typically the one displayed on the homepage product card, search results, and cart.
    @Column(name = "is_primary", nullable = false)
    private boolean primary = false;

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


//1. image_type — What kind of image is this?
//It identifies the purpose of the image.
//
//We defined three values:
//Value	Meaning	Example
//MAIN	Original artwork/product image	Shiva artwork
//GALLERY	Additional product image	Close-up of canvas texture
//MOCKUP	Artwork shown in a realistic setting	Shiva artwork on a living-room wall
//
//In SQL:
//image_type VARCHAR(20) NOT NULL DEFAULT 'GALLERY',
//
//CONSTRAINT chk_product_images_type
//    CHECK (image_type IN ('MAIN', 'GALLERY', 'MOCKUP'))
//
//
//If we don't provide an image_type, PostgreSQL defaults it to GALLERY.
//
//If we try to insert image_type = 'VIDEO', PostgreSQL rejects it because our check constraint doesn't allow that value.
//2. sort_order — In which order should images appear?
//This controls the sequence of images displayed in our React product gallery.
//
//For example, we want the original artwork first, followed by room mockups.
//Image	sort_order
//Original Shiva artwork	0
//Living room mockup	1
//Bedroom mockup	2
//Pooja room mockup	3
//Hallway mockup	4
//Office mockup	5
//
//Try changing the image order
//0
//
//Original
//
//
//1
//
//Living room
//
//
//2
//
//Bedroom
//
//
//3
//
//Pooja room
//
//
//4
//
//Hallway
//
//
//5
//
//Office
//
//
//
//The numbers represent the sort_order values we'd save in PostgreSQL. This is a UI demonstration only.
//Our backend can retrieve images in this order:
//SELECT *
//FROM product_images
//WHERE product_id = 'your-product-uuid'
//ORDER BY sort_order ASC, id ASC;
//
//
//Important: sort_order is not unique. Multiple images can have the same value, which is why we can use id as a secondary ordering column.
//3. is_primary — Which image represents the product?
//This is a boolean (TRUE or FALSE).
//
//The primary image is typically the one displayed on the homepage product card, search results, and cart.
//
//For example:
//Image	image_type	is_primary
//Original Shiva artwork	MAIN	TRUE
//Living room mockup	MOCKUP	FALSE
//Bedroom mockup	MOCKUP	FALSE
//Close-up image	GALLERY	FALSE
//
//We created this index:
//CREATE UNIQUE INDEX uk_product_images_primary
//ON public.product_images(product_id)
//WHERE is_primary = TRUE;
//
//
//This means one product can have at most one primary image.
//- P101 → Original Shiva image is primary.
//- P102 → Original Hanuman image is primary.
//- P101 cannot have two images marked is_primary = TRUE.
//Notice that image_type = 'MAIN' and is_primary = TRUE are technically separate concepts. Our current SQL doesn't force every MAIN image to be primary or prevent a MOCKUP image from being primary.
//
//For our application, we'll follow the convention that the original artwork is the primary image.
//4. alt_text — What does the image show?
//alt_text is a short text description of the image, primarily used for accessibility.
//
//For example:
//Lord Shiva meditating in the Himalayas with a golden sunset
//
//
//In React:
//<img
//  src="/art/art1.jpg"
//  alt="Lord Shiva meditating in the Himalayas with a golden sunset"
/// >
//
//
//Screen readers can read this description to users who cannot see the image. It can also provide context when images don't load.
//
//Examples:
//Image	alt_text
//Original Shiva artwork	Lord Shiva meditating in the Himalayas
//Living room mockup	Framed Shiva artwork above a beige living-room sofa
//Bedroom mockup	Shiva canvas print displayed above a bedroom bed
//
//Our SQL column:
//alt_text VARCHAR(255)
//
//
//It is nullable, but we should populate it for meaningful product images. For purely decorative images, an empty HTML alt attribute may be appropriate.
//
//5. How all four work together
//Here is what our product_images table could contain for one artwork.
//image_url	image_type	sort_order	is_primary
///art/art1.jpg	MAIN	0	TRUE
///rooms/living.jpg	MOCKUP	1	FALSE
///rooms/bedroom.jpg	MOCKUP	2	FALSE
///rooms/pooja.jpg	MOCKUP	3	FALSE
///rooms/hallway.jpg	MOCKUP	4	FALSE
//
//Each row also has its own id, product_id, alt_text, and timestamps.
//
//One important detail for our current website: the files in /rooms/ are reusable empty-room templates. If React overlays the selected artwork dynamically, these templates don't need to be duplicated for every product. Later, we can model shared room templates separately from product-specific images.
//
//Quick way to remember:
//- image_type → What is this image?
//- sort_order → When does it appear in the gallery sequence?
//- is_primary → Which image represents the product?
//- alt_text → What does the image depict?
