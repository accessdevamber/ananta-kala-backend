CREATE TABLE public.orders (
    id UUID PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL,
    cart_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    subtotal NUMERIC(12,2) NOT NULL,
    shipping_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(12,2) NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'INR',
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_orders_order_number
        UNIQUE (order_number),

    --This ensures that one Cart cannot generate multiple Orders.
    CONSTRAINT uk_orders_cart_id
        UNIQUE (cart_id),

    --Once a Cart is referenced by an Order, PostgreSQL will prevent that Cart from being deleted.
    --This preserves the relationship between the original Cart and the resulting Order.
    CONSTRAINT fk_orders_cart
        FOREIGN KEY (cart_id)
        REFERENCES public.carts(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_orders_order_number
        CHECK (TRIM(order_number) <> ''),

    CONSTRAINT chk_orders_status
        CHECK (status IN (
            'PENDING',
            'CONFIRMED',
            'PROCESSING',
            'SHIPPED',
            'COMPLETED',
            'CANCELLED'
        )),

    CONSTRAINT chk_orders_amounts
        CHECK (
            subtotal >= 0
            AND shipping_amount >= 0
            AND discount_amount >= 0
            AND total_amount >= 0
        ),

    CONSTRAINT chk_orders_total_amount
        CHECK (
            total_amount = subtotal
                         + shipping_amount
                         - discount_amount
        ),

    CONSTRAINT chk_orders_currency
        CHECK (currency = 'INR')
);

CREATE TABLE public.order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    product_variant_id UUID NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    sku VARCHAR(80) NOT NULL,
    product_type VARCHAR(30) NOT NULL,
    size_label VARCHAR(50),
    frame_finish VARCHAR(50),
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL,
    line_total NUMERIC(12,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id)
        REFERENCES public.orders(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_order_items_variant
        FOREIGN KEY (product_variant_id)
        REFERENCES public.product_variants(id)
        ON DELETE RESTRICT,

    --This is similar to our CartItem unique constraint.
--      order_id	product_variant_id	quantity	Allowed?
--      ORDER-101	SHIVA-V1	        2	            Yes
--      ORDER-101	SHIVA-V2	        1	            Yes
--      ORDER-102	SHIVA-V1	        3	            Yes
--      ORDER-101	SHIVA-V1	        4	            No
--
--      The same ProductVariant can exist in multiple Orders, but it cannot appear in multiple OrderItem rows within the same Order.
    CONSTRAINT uk_order_items_order_variant
        UNIQUE (order_id, product_variant_id),

    CONSTRAINT chk_order_items_product_name
        CHECK (TRIM(product_name) <> ''),

    CONSTRAINT chk_order_items_sku
        CHECK (TRIM(sku) <> ''),

    CONSTRAINT chk_order_items_product_type
        CHECK (product_type IN (
            'FRAMED_CANVAS',
            'DIGITAL_DOWNLOAD'
        )),

    CONSTRAINT chk_order_items_variant_details
        CHECK (
            (
                product_type = 'FRAMED_CANVAS'
                AND size_label IS NOT NULL
                AND TRIM(size_label) <> ''
                AND frame_finish IS NOT NULL
                AND TRIM(frame_finish) <> ''
            )
            OR
            (
                product_type = 'DIGITAL_DOWNLOAD'
                AND size_label IS NULL
                AND frame_finish IS NULL
            )
        ),

    CONSTRAINT chk_order_items_quantity
        CHECK (quantity > 0),

    CONSTRAINT chk_order_items_unit_price
        CHECK (unit_price >= 0),

    CONSTRAINT chk_order_items_line_total
        CHECK (
            line_total >= 0
            AND line_total = unit_price * quantity
        )
);

CREATE INDEX idx_orders_status
    ON public.orders(status);

CREATE INDEX idx_orders_created_at
    ON public.orders(created_at);

CREATE INDEX idx_order_items_product_variant_id
    ON public.order_items(product_variant_id);