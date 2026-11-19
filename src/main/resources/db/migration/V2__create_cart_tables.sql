
-- V2__create_cart_tables.sql

-- =====================================================
-- 1. CARTS
-- =====================================================

CREATE TABLE public.carts (

    id UUID PRIMARY KEY,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT chk_carts_status
        CHECK (status IN (
            'ACTIVE',
            'CHECKED_OUT',
            'ABANDONED'
        ))

);


-- =====================================================
-- 2. CART ITEMS
-- =====================================================

CREATE TABLE public.cart_items (

    id UUID PRIMARY KEY,

    cart_id UUID NOT NULL,

    product_variant_id UUID NOT NULL,

    quantity INTEGER NOT NULL DEFAULT 1,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    -- Each CartItem belongs to one Cart
    CONSTRAINT fk_cart_items_cart
        FOREIGN KEY (cart_id)
        REFERENCES public.carts(id)
        ON DELETE CASCADE,

    -- Each CartItem references one ProductVariant
    CONSTRAINT fk_cart_items_variant
        FOREIGN KEY (product_variant_id)
        REFERENCES public.product_variants(id)
        ON DELETE RESTRICT,

    -- Quantity must be positive
    CONSTRAINT chk_cart_items_quantity
        CHECK (quantity > 0),

    -- One variant can appear only once per Cart
    CONSTRAINT uk_cart_items_cart_variant
        UNIQUE (cart_id, product_variant_id)

);


-- =====================================================
-- 3. INDEXES
-- =====================================================

-- Find all CartItems belonging to a Cart.
-- The UNIQUE constraint above already creates an index
-- starting with cart_id, so no separate cart_id index
-- is needed for now.

-- Find CartItems referencing a ProductVariant.
CREATE INDEX idx_cart_items_product_variant_id
    ON public.cart_items(product_variant_id);

-- Optional: useful for filtering carts by status
CREATE INDEX idx_carts_status
    ON public.carts(status);
