
-- V1__create_catalog_tables.sql

CREATE TABLE public.categories (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(120) NOT NULL,
    description TEXT,
    parent_id UUID,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_categories_slug UNIQUE (slug),

    CONSTRAINT fk_categories_parent
        FOREIGN KEY (parent_id)
        REFERENCES public.categories(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_categories_not_self_parent
        CHECK (parent_id IS NULL OR parent_id <> id),

    CONSTRAINT chk_categories_name_not_blank
        CHECK (LENGTH(TRIM(name)) > 0)
);


CREATE TABLE public.products (
    id UUID PRIMARY KEY,
    category_id UUID NOT NULL,

    name VARCHAR(200) NOT NULL,
    slug VARCHAR(220) NOT NULL,
    description TEXT,

    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_products_slug UNIQUE (slug),

    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id)
        REFERENCES public.categories(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_products_name_not_blank
        CHECK (LENGTH(TRIM(name)) > 0),

    CONSTRAINT chk_products_status
        CHECK (status IN (
            'DRAFT',
            'ACTIVE',
            'INACTIVE',
            'DISCONTINUED'
        ))
);


CREATE TABLE public.product_images (
    id UUID PRIMARY KEY,

    product_id UUID NOT NULL,

    image_url TEXT NOT NULL,
    image_type VARCHAR(20) NOT NULL DEFAULT 'GALLERY',

    alt_text VARCHAR(255),
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_product_images_product
        FOREIGN KEY (product_id)
        REFERENCES public.products(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_product_images_type
        CHECK (image_type IN ('MAIN', 'GALLERY', 'MOCKUP')),

    CONSTRAINT chk_product_images_sort_order
        CHECK (sort_order >= 0),

    CONSTRAINT chk_product_images_url_not_blank
        CHECK (LENGTH(TRIM(image_url)) > 0)
);


CREATE TABLE public.product_variants (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL,

    sku VARCHAR(80) NOT NULL,

    product_type VARCHAR(30) NOT NULL,
    size_label VARCHAR(30),
    frame_finish VARCHAR(30),

    price NUMERIC(12,2) NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'INR',

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_product_variants_sku UNIQUE (sku),

    CONSTRAINT fk_product_variants_product
        FOREIGN KEY (product_id)
        REFERENCES public.products(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_variant_price
        CHECK (price >= 0),

    CONSTRAINT chk_variant_sku_not_blank
        CHECK (LENGTH(TRIM(sku)) > 0),

    CONSTRAINT chk_variant_type
        CHECK (product_type IN (
            'FRAMED_CANVAS',
            'DIGITAL_DOWNLOAD'
        )),

    CONSTRAINT chk_variant_configuration
        CHECK (
            (
                product_type = 'FRAMED_CANVAS'
                AND size_label IS NOT NULL
                AND frame_finish IS NOT NULL
            )
            OR
            (
                product_type = 'DIGITAL_DOWNLOAD'
                AND size_label IS NULL
                AND frame_finish IS NULL
            )
        )
);


-- Indexes

CREATE INDEX idx_categories_parent_id
    ON public.categories(parent_id);

CREATE INDEX idx_products_category_id
    ON public.products(category_id);

CREATE INDEX idx_products_status
    ON public.products(status);

CREATE INDEX idx_variants_product_id
    ON public.product_variants(product_id);

CREATE INDEX idx_variants_active
    ON public.product_variants(active);

-- Index for fetching images belonging to a product
CREATE INDEX idx_product_images_product_id
    ON public.product_images(product_id);

-- Ensure only one primary image per product
CREATE UNIQUE INDEX uk_product_images_primary
    ON public.product_images(product_id)
    WHERE is_primary = TRUE;
